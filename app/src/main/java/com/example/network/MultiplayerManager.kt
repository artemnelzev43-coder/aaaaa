package com.example.network

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class MultiplayerManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isHost = MutableStateFlow(false)
    val isHost: StateFlow<Boolean> = _isHost.asStateFlow()

    private val _currentRoom = MutableStateFlow<RoomInfo?>(null)
    val currentRoom: StateFlow<RoomInfo?> = _currentRoom.asStateFlow()

    private val _discoveredRooms = MutableStateFlow<List<RoomInfo>>(emptyList())
    val discoveredRooms: StateFlow<List<RoomInfo>> = _discoveredRooms.asStateFlow()

    private val _isSearchingWifi = MutableStateFlow(false)
    val isSearchingWifi: StateFlow<Boolean> = _isSearchingWifi.asStateFlow()

    private val _connectionStatus = MutableStateFlow<String>("Не подключен")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private val _networkPlayers = MutableStateFlow<List<PlayerNetworkState>>(emptyList())
    val networkPlayers: StateFlow<List<PlayerNetworkState>> = _networkPlayers.asStateFlow()

    private val connectedClients = ConcurrentHashMap<String, ConnectedClient>()
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var clientWriter: PrintWriter? = null

    private var broadcastJob: Job? = null
    private var discoveryJob: Job? = null
    private var serverAcceptJob: Job? = null
    private var clientReaderJob: Job? = null
    private var syncJob: Job? = null

    private var localPlayerState = PlayerNetworkState(
        playerId = "p_${System.currentTimeMillis() % 10000}",
        playerName = "Vanguard_SpecOps",
        x = 2.5f,
        y = 2.5f,
        angle = 0.8f,
        hp = 100,
        isShooting = false,
        isTeamDelta = true
    )

    companion object {
        const val BROADCAST_PORT = 8888
        const val TCP_PORT = 9999
        private const val TAG = "RealMultiplayer"
    }

    data class ConnectedClient(
        val socket: Socket,
        val writer: PrintWriter,
        var playerState: PlayerNetworkState
    )

    fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    fun getLocalIpAddress(): String {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val ipInt = wifiManager?.connectionInfo?.ipAddress ?: 0
            if (ipInt != 0) {
                String.format(
                    java.util.Locale.US,
                    "%d.%d.%d.%d",
                    ipInt and 0xff,
                    ipInt shr 8 and 0xff,
                    ipInt shr 16 and 0xff,
                    ipInt shr 24 and 0xff
                )
            } else {
                "127.0.0.1"
            }
        } catch (_: Exception) {
            "127.0.0.1"
        }
    }

    fun updateLocalPlayerState(x: Float, y: Float, angle: Float, hp: Int, isShooting: Boolean) {
        localPlayerState = localPlayerState.copy(
            x = x,
            y = y,
            angle = angle,
            hp = hp,
            isShooting = isShooting
        )

        // If client, send update to host
        if (!_isHost.value && clientWriter != null) {
            scope.launch {
                try {
                    clientWriter?.println("POS|${localPlayerState.playerId}|${localPlayerState.playerName}|$x|$y|$angle|$hp|$isShooting|${localPlayerState.isTeamDelta}")
                } catch (_: Exception) {}
            }
        }

        // If host, broadcast to all connected clients
        if (_isHost.value) {
            broadcastToClients("POS|${localPlayerState.playerId}|${localPlayerState.playerName}|$x|$y|$angle|$hp|$isShooting|${localPlayerState.isTeamDelta}")
            refreshPlayersList()
        }
    }

    // 1. Create Room (Host real ServerSocket & UDP broadcast)
    fun createRoom(hostName: String, onCreated: (RoomInfo) -> Unit) {
        scope.launch {
            leaveCurrentRoom()

            val code = generateRoomCode()
            val ip = getLocalIpAddress()
            val room = RoomInfo(
                roomCode = code,
                hostName = hostName,
                hostIp = ip,
                port = TCP_PORT,
                playerCount = 1,
                maxPlayers = 4,
                mapName = "Кибер-Арена (LAN)",
                pingMs = 1
            )
            _isHost.value = true
            _currentRoom.value = room
            localPlayerState = localPlayerState.copy(playerName = hostName, isTeamDelta = true)
            _connectionStatus.value = "Комната #$code создана на $ip:$TCP_PORT"
            refreshPlayersList()

            try {
                serverSocket?.close()
                serverSocket = ServerSocket()
                serverSocket?.reuseAddress = true
                serverSocket?.bind(InetSocketAddress(TCP_PORT))
            } catch (e: Exception) {
                Log.e(TAG, "ServerSocket bind error: ${e.message}")
            }

            startUdpBeaconBroadcast(room)
            startServerAcceptLoop(code)

            withContext(Dispatchers.Main) {
                onCreated(room)
            }
        }
    }

    private fun startUdpBeaconBroadcast(room: RoomInfo) {
        broadcastJob?.cancel()
        broadcastJob = scope.launch {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket()
                socket.broadcast = true
                val dest = InetAddress.getByName("255.255.255.255")

                while (isActive && _isHost.value) {
                    val count = 1 + connectedClients.size
                    val msg = "VANGUARD_ROOM|${room.roomCode}|${room.hostName}|${room.hostIp}|${room.port}|$count|${room.maxPlayers}"
                    val bytes = msg.toByteArray()
                    val packet = DatagramPacket(bytes, bytes.size, dest, BROADCAST_PORT)
                    socket.send(packet)
                    delay(1200L)
                }
            } catch (e: Exception) {
                Log.e(TAG, "UDP Broadcast error: ${e.message}")
            } finally {
                socket?.close()
            }
        }
    }

    private fun startServerAcceptLoop(expectedRoomCode: String) {
        serverAcceptJob?.cancel()
        serverAcceptJob = scope.launch {
            while (isActive && _isHost.value) {
                try {
                    val socket = serverSocket?.accept() ?: break
                    launch {
                        handleIncomingClient(socket, expectedRoomCode)
                    }
                } catch (_: Exception) {
                    break
                }
            }
        }
    }

    private suspend fun handleIncomingClient(socket: Socket, expectedRoomCode: String) {
        var clientReader: BufferedReader? = null
        var writer: PrintWriter? = null
        var clientId = ""

        try {
            clientReader = BufferedReader(InputStreamReader(socket.getInputStream()))
            writer = PrintWriter(socket.getOutputStream(), true)

            // Read Handshake
            val handshake = clientReader.readLine() ?: return
            val parts = handshake.split("|")
            if (parts.size >= 4 && parts[0] == "CONNECT") {
                val reqCode = parts[1].trim().uppercase()
                val reqPlayerId = parts[2]
                val reqPlayerName = parts[3]
                val isTeamDelta = (connectedClients.size % 2 == 1) // Balance teams

                if (reqCode != expectedRoomCode.trim().uppercase()) {
                    writer.println("REJECT|Неверный код комнаты ($reqCode)")
                    socket.close()
                    return
                }

                clientId = reqPlayerId
                val newPlayer = PlayerNetworkState(
                    playerId = reqPlayerId,
                    playerName = reqPlayerName,
                    x = 13.5f - (connectedClients.size * 1.5f),
                    y = 12.5f,
                    angle = 3.14f,
                    hp = 100,
                    isShooting = false,
                    isTeamDelta = isTeamDelta
                )

                connectedClients[clientId] = ConnectedClient(socket, writer, newPlayer)
                writer.println("ACCEPT|${expectedRoomCode}|${localPlayerState.playerName}|$isTeamDelta")
                _connectionStatus.value = "Игрок $reqPlayerName подключился!"
                refreshPlayersList()

                // Inform all about new player
                broadcastToClients("PLAYER_JOIN|${newPlayer.playerId}|${newPlayer.playerName}|${newPlayer.x}|${newPlayer.y}|${newPlayer.angle}|${newPlayer.hp}|${newPlayer.isTeamDelta}")

                // Read incoming packets from this client
                var line: String?
                while (clientReader.readLine().also { line = it } != null) {
                    val msg = line ?: break
                    processHostIncomingMessage(clientId, msg)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Client connection handler error: ${e.message}")
        } finally {
            if (clientId.isNotEmpty()) {
                val removed = connectedClients.remove(clientId)
                _connectionStatus.value = "Игрок ${removed?.playerState?.playerName ?: clientId} отключился"
                refreshPlayersList()
                broadcastToClients("PLAYER_LEAVE|$clientId")
            }
            try {
                socket.close()
            } catch (_: Exception) {}
        }
    }

    private fun processHostIncomingMessage(senderId: String, msg: String) {
        val parts = msg.split("|")
        if (parts.isEmpty()) return
        when (parts[0]) {
            "POS" -> {
                if (parts.size >= 8) {
                    val pId = parts[1]
                    val pName = parts[2]
                    val x = parts[3].toFloatOrNull() ?: 2.5f
                    val y = parts[4].toFloatOrNull() ?: 2.5f
                    val angle = parts[5].toFloatOrNull() ?: 0f
                    val hp = parts[6].toIntOrNull() ?: 100
                    val isShooting = parts[7].toBooleanStrictOrNull() ?: false
                    val isDelta = if (parts.size >= 9) parts[8].toBooleanStrictOrNull() ?: true else true

                    val client = connectedClients[pId]
                    if (client != null) {
                        client.playerState = client.playerState.copy(
                            x = x,
                            y = y,
                            angle = angle,
                            hp = hp,
                            isShooting = isShooting,
                            isTeamDelta = isDelta
                        )
                    }
                    refreshPlayersList()
                    // Forward to all OTHER clients
                    broadcastToOthers(senderId, msg)
                }
            }
            "HIT" -> {
                broadcastToClients(msg)
            }
            "RADIO" -> {
                broadcastToClients(msg)
            }
        }
    }

    private fun broadcastToClients(msg: String) {
        connectedClients.values.forEach { client ->
            try {
                client.writer.println(msg)
            } catch (_: Exception) {}
        }
    }

    private fun broadcastToOthers(excludeId: String, msg: String) {
        connectedClients.forEach { (id, client) ->
            if (id != excludeId) {
                try {
                    client.writer.println(msg)
                } catch (_: Exception) {}
            }
        }
    }

    private fun refreshPlayersList() {
        val list = mutableListOf<PlayerNetworkState>()
        list.add(localPlayerState)
        list.addAll(connectedClients.values.map { it.playerState })
        _networkPlayers.value = list
        _currentRoom.value = _currentRoom.value?.copy(playerCount = list.size)
    }

    // 2. Search Real WiFi Rooms via UDP (No fake mock rooms)
    fun startWifiRoomSearch() {
        _isSearchingWifi.value = true
        _discoveredRooms.value = emptyList()

        discoveryJob?.cancel()
        discoveryJob = scope.launch {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket(null)
                socket.reuseAddress = true
                socket.bind(InetSocketAddress(BROADCAST_PORT))
                socket.soTimeout = 2000
                val buffer = ByteArray(512)
                val packet = DatagramPacket(buffer, buffer.size)

                val roomsMap = mutableMapOf<String, RoomInfo>()

                while (isActive && _isSearchingWifi.value) {
                    try {
                        socket.receive(packet)
                        val msg = String(packet.data, 0, packet.length)
                        val parts = msg.split("|")
                        if (parts.size >= 6 && parts[0] == "VANGUARD_ROOM") {
                            val rCode = parts[1]
                            val rHost = parts[2]
                            val rIp = packet.address.hostAddress ?: parts[3]
                            val rPort = parts[4].toIntOrNull() ?: TCP_PORT
                            val rCount = parts[5].toIntOrNull() ?: 1
                            val rMax = if (parts.size >= 7) parts[6].toIntOrNull() ?: 4 else 4

                            val info = RoomInfo(
                                roomCode = rCode,
                                hostName = rHost,
                                hostIp = rIp,
                                port = rPort,
                                playerCount = rCount,
                                maxPlayers = rMax,
                                pingMs = Random.nextInt(12, 35)
                            )
                            roomsMap[rCode] = info
                            _discoveredRooms.value = roomsMap.values.toList()
                        }
                    } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Discovery error: ${e.message}")
            } finally {
                socket?.close()
            }
        }
    }

    fun stopWifiRoomSearch() {
        _isSearchingWifi.value = false
        discoveryJob?.cancel()
    }

    // 3. Join Discovered Room
    fun joinDiscoveredRoom(room: RoomInfo, playerName: String, onResult: (Boolean, String) -> Unit) {
        scope.launch {
            joinRoomAtAddress(room.hostIp, room.port, room.roomCode, playerName, onResult)
        }
    }

    // 4. Join by Room Code: Strictly connects to existing host or returns error (NO fake dummy rooms!)
    fun joinRoomByCode(code: String, playerName: String, onResult: (Boolean, String) -> Unit) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.length < 3) {
            onResult(false, "Код комнаты должен содержать не менее 3 символов!")
            return
        }

        scope.launch {
            _connectionStatus.value = "Поиск комнаты #$cleanCode в сети Wi-Fi..."

            // Check if room was discovered via UDP
            val discovered = _discoveredRooms.value.find { it.roomCode.equals(cleanCode, ignoreCase = true) }
            if (discovered != null) {
                joinRoomAtAddress(discovered.hostIp, discovered.port, cleanCode, playerName, onResult)
                return@launch
            }

            // Attempt local loopback or standard gateway connection
            val myIp = getLocalIpAddress()
            val candidateIps = mutableListOf("127.0.0.1")
            if (myIp != "127.0.0.1") {
                candidateIps.add(myIp)
            }

            var connected = false
            for (ip in candidateIps) {
                val success = tryConnectToHost(ip, TCP_PORT, cleanCode, playerName)
                if (success) {
                    connected = true
                    withContext(Dispatchers.Main) {
                        onResult(true, "Успешное подключение к комнате #$cleanCode!")
                    }
                    break
                }
            }

            if (!connected) {
                withContext(Dispatchers.Main) {
                    _connectionStatus.value = "Комната #$cleanCode не найдена в сети"
                    onResult(
                        false,
                        "Комната #$cleanCode не найдена! Убедитесь, что хост создал комнату и оба устройства находятся в одной Wi-Fi сети."
                    )
                }
            }
        }
    }

    private suspend fun joinRoomAtAddress(
        hostIp: String,
        port: Int,
        roomCode: String,
        playerName: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val success = tryConnectToHost(hostIp, port, roomCode, playerName)
        withContext(Dispatchers.Main) {
            if (success) {
                onResult(true, "Подключено к комнате #$roomCode ($hostIp)!")
            } else {
                onResult(false, "Не удалось подключиться к хосту $hostIp:$port. Проверьте соединение.")
            }
        }
    }

    private suspend fun tryConnectToHost(
        hostIp: String,
        port: Int,
        roomCode: String,
        playerName: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            leaveCurrentRoom()
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(hostIp, port), 2500)
                val writer = PrintWriter(socket.getOutputStream(), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                // Send Handshake
                val myId = "p_${System.currentTimeMillis() % 10000}"
                writer.println("CONNECT|$roomCode|$myId|$playerName|false")

                val response = reader.readLine()
                if (response != null && response.startsWith("ACCEPT")) {
                    val respParts = response.split("|")
                    val hostName = if (respParts.size >= 3) respParts[2] else "Host"
                    val isDelta = if (respParts.size >= 4) respParts[3].toBooleanStrictOrNull() ?: false else false

                    clientSocket = socket
                    clientWriter = writer
                    _isHost.value = false
                    localPlayerState = localPlayerState.copy(
                        playerId = myId,
                        playerName = playerName,
                        isTeamDelta = isDelta,
                        x = 13.5f,
                        y = 12.5f
                    )

                    val room = RoomInfo(
                        roomCode = roomCode,
                        hostName = hostName,
                        hostIp = hostIp,
                        port = port,
                        playerCount = 2,
                        maxPlayers = 4
                    )
                    _currentRoom.value = room
                    _connectionStatus.value = "Подключено к хосту $hostName (#$roomCode)"

                    // Start Client Reader Loop
                    startClientReaderLoop(reader)
                    return@withContext true
                } else {
                    socket.close()
                    return@withContext false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Connection failed to $hostIp:$port - ${e.message}")
                return@withContext false
            }
        }
    }

    private fun startClientReaderLoop(reader: BufferedReader) {
        clientReaderJob?.cancel()
        clientReaderJob = scope.launch {
            val remotePlayers = mutableMapOf<String, PlayerNetworkState>()
            try {
                var line: String? = null
                while (isActive && reader.readLine().also { line = it } != null) {
                    val msg = line ?: break
                    val parts = msg.split("|")
                    if (parts.isEmpty()) continue

                    when (parts[0]) {
                        "POS" -> {
                            if (parts.size >= 8) {
                                val pId = parts[1]
                                val pName = parts[2]
                                val x = parts[3].toFloatOrNull() ?: 2.5f
                                val y = parts[4].toFloatOrNull() ?: 2.5f
                                val angle = parts[5].toFloatOrNull() ?: 0f
                                val hp = parts[6].toIntOrNull() ?: 100
                                val isShooting = parts[7].toBooleanStrictOrNull() ?: false
                                val isDelta = if (parts.size >= 9) parts[8].toBooleanStrictOrNull() ?: true else true

                                if (pId != localPlayerState.playerId) {
                                    remotePlayers[pId] = PlayerNetworkState(
                                        playerId = pId,
                                        playerName = pName,
                                        x = x,
                                        y = y,
                                        angle = angle,
                                        hp = hp,
                                        isShooting = isShooting,
                                        isTeamDelta = isDelta
                                    )
                                    val list = mutableListOf<PlayerNetworkState>()
                                    list.add(localPlayerState)
                                    list.addAll(remotePlayers.values)
                                    _networkPlayers.value = list
                                    _currentRoom.value = _currentRoom.value?.copy(playerCount = list.size)
                                }
                            }
                        }
                        "PLAYER_LEAVE" -> {
                            if (parts.size >= 2) {
                                val pId = parts[1]
                                remotePlayers.remove(pId)
                                val list = mutableListOf<PlayerNetworkState>()
                                list.add(localPlayerState)
                                list.addAll(remotePlayers.values)
                                _networkPlayers.value = list
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun sendHitEvent(targetId: String, damage: Int, isCrit: Boolean) {
        scope.launch {
            val msg = "HIT|${localPlayerState.playerId}|$targetId|$damage|$isCrit"
            if (_isHost.value) {
                broadcastToClients(msg)
            } else {
                clientWriter?.println(msg)
            }
        }
    }

    fun leaveCurrentRoom() {
        broadcastJob?.cancel()
        serverAcceptJob?.cancel()
        clientReaderJob?.cancel()
        discoveryJob?.cancel()

        try {
            clientSocket?.close()
            clientSocket = null
            clientWriter = null
            serverSocket?.close()
            serverSocket = null
        } catch (_: Exception) {}

        connectedClients.clear()
        _isHost.value = false
        _currentRoom.value = null
        _networkPlayers.value = emptyList()
        _connectionStatus.value = "Не подключен"
    }
}
