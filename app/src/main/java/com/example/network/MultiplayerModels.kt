package com.example.network

data class RoomInfo(
    val roomCode: String,
    val hostName: String,
    val hostIp: String,
    val port: Int,
    val playerCount: Int,
    val maxPlayers: Int = 4,
    val mapName: String = "Кибер-Арена A/B",
    val pingMs: Int = 18
)

data class PlayerNetworkState(
    val playerId: String,
    val playerName: String,
    val x: Float,
    val y: Float,
    val angle: Float,
    val hp: Int,
    val isShooting: Boolean = false,
    val isTeamDelta: Boolean = true
)
