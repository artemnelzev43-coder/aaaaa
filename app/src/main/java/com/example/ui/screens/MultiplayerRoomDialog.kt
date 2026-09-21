package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiFind
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.network.RoomInfo
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@Composable
fun MultiplayerRoomDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val multiplayer = viewModel.multiplayerManager
    val profile by viewModel.playerProfile.collectAsState()
    val currentRoom by multiplayer.currentRoom.collectAsState()
    val isHost by multiplayer.isHost.collectAsState()
    val discoveredRooms by multiplayer.discoveredRooms.collectAsState()
    val isSearchingWifi by multiplayer.isSearchingWifi.collectAsState()
    val connectionStatus by multiplayer.connectionStatus.collectAsState()
    val networkPlayers by multiplayer.networkPlayers.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Поиск WiFi, 1: По коду, 2: Создать
    var roomCodeInput by remember { mutableStateOf("") }
    var joinStatusFeedback by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            multiplayer.stopWifiRoomSearch()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, Color(0xFF00E5FF), RoundedCornerShape(20.dp)),
            color = Color(0xFF0D1524)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "МУЛЬТИПЛЕЕР (LAN / WI-FI)",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.Gray)
                    }
                }

                // If currently inside an active room
                if (currentRoom != null) {
                    val room = currentRoom!!
                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("КОД КОМНАТЫ:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(room.roomCode, color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 22.sp)
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(room.roomCode))
                                        joinStatusFeedback = "Код ${room.roomCode} скопирован в буфер!"
                                    },
                                    modifier = Modifier.size(36.dp).background(Color(0xFF334155), CircleShape)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Копировать код", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Хост: ${room.hostName} (${room.hostIp}:${room.port})", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("Статус: $connectionStatus", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("ИГРОКИ В ЛОББИ (${networkPlayers.size}/${room.maxPlayers}):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            networkPlayers.forEach { p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        p.playerName,
                                        color = if (p.isTeamDelta) Color(0xFF00E5FF) else Color(0xFFFF1744),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text("READY", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { multiplayer.leaveCurrentRoom() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(44.dp)
                                ) {
                                    Text("ВЫЙТИ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        onDismiss()
                                        viewModel.startMultiplayerMatch()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1.4f).height(44.dp).testTag("start_multiplayer_match_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("НАЧАТЬ МАТЧ", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    // Navigation Tabs
                    Spacer(modifier = Modifier.height(10.dp))
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFF00E5FF),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color(0xFF00E5FF)
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                multiplayer.startWifiRoomSearch()
                            },
                            text = { Text("ПОИСК В WI-FI", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("ПО КОДУ", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("СОЗДАТЬ", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    when (selectedTab) {
                        0 -> {
                            // WiFi Room Search
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Комнаты в локальной сети:", color = Color.Gray, fontSize = 12.sp)
                                IconButton(
                                    onClick = { multiplayer.startWifiRoomSearch() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Обновить", tint = Color(0xFF00E5FF))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (discoveredRooms.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .background(Color(0xFF131C2E), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = Color(0xFF00E5FF), modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Сканирование UDP пакетов в сети...", color = Color.Gray, fontSize = 11.sp)
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.height(160.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(discoveredRooms) { room ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("Комната #${room.roomCode}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("Хост: ${room.hostName} • ${room.playerCount}/${room.maxPlayers} • Ping ${room.pingMs}ms", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                                }
                                                Button(
                                                    onClick = {
                                                        multiplayer.joinRoomByCode(room.roomCode, profile.name) { success, msg ->
                                                            joinStatusFeedback = msg
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.height(34.dp).testTag("join_room_${room.roomCode}_button")
                                                ) {
                                                    Text("ПОДКЛ", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Join by Code
                            Column {
                                Text("Введите 6-значный код комнаты хоста:", color = Color.Gray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = roomCodeInput,
                                    onValueChange = { roomCodeInput = it.uppercase() },
                                    placeholder = { Text("Пример: 8X7KL9", color = Color.Gray) },
                                    modifier = Modifier.fillMaxWidth().testTag("multiplayer_room_code_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00E5FF),
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        multiplayer.joinRoomByCode(roomCodeInput, profile.name) { success, msg ->
                                            joinStatusFeedback = msg
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("connect_by_code_button")
                                ) {
                                    Icon(Icons.Default.Login, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ПОДКЛЮЧИТЬСЯ ПО КОДУ", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                            }
                        }
                        2 -> {
                            // Create Room
                            Column {
                                Text("Создание лобби для игроков в вашей сети WiFi:", color = Color.Gray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = Color(0xFF1A263A),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Имя хоста: ${profile.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Карта: Кибер-Арена A/B (Разрушаемые ящики)", color = Color(0xFF00E5FF), fontSize = 11.sp)
                                        Text("Слоты: 4 игрока (2 vs 2)", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    }
                                }
                                Button(
                                    onClick = {
                                        multiplayer.createRoom(profile.name) { room ->
                                            joinStatusFeedback = "Комната создана! Код: ${room.roomCode}"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("create_room_submit_button")
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("СОЗДАТЬ КОМНАТУ", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    joinStatusFeedback?.let { feedback ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(feedback, color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
