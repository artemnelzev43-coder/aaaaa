package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardPlayer
import com.example.ui.viewmodel.GameViewModel

@Composable
fun LeaderboardScreen(viewModel: GameViewModel) {
    val players by viewModel.leaderboard.collectAsState()
    var selectedFilter by remember { mutableStateOf("Глобальный") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A0F))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ТАБЛИЦА ЛИДЕРОВ", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text("Рейтинг лучших стрелков в реальном времени", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
            Icon(Icons.Default.EmojiEvents, contentDescription = "Трофей", tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Pills
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Глобальный", "Кланы", "Друзья").forEach { tab ->
                val isSelected = tab == selectedFilter
                Surface(
                    onClick = { selectedFilter = tab },
                    color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = tab,
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Top 3 Podium Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val top1 = players.find { it.rank == 1 }
            val top2 = players.find { it.rank == 2 }
            val top3 = players.find { it.rank == 3 }

            top2?.let { PodiumCard(player = it, medal = "🥈", color = Color(0xFFB0BEC5), modifier = Modifier.weight(1f)) }
            top1?.let { PodiumCard(player = it, medal = "🥇", color = Color(0xFFFFD700), modifier = Modifier.weight(1.1f)) }
            top3?.let { PodiumCard(player = it, medal = "🥉", color = Color(0xFFCD7F32), modifier = Modifier.weight(1f)) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Players List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(players) { p ->
                val isCurrent = p.isCurrentPlayer
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) Color(0xFF11253C) else Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isCurrent) 1.5.dp else 1.dp,
                        if (isCurrent) Color(0xFF00E5FF) else Color(0xFF1E293B)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Rank number
                            Text(
                                text = "#${p.rank}",
                                color = if (p.rank <= 3) Color(0xFFFFD700) else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                modifier = Modifier.width(32.dp)
                            )
                            // Platform Icon
                            Text(p.platform.iconText, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isCurrent) "${p.name} (Вы)" else p.name,
                                        color = if (isCurrent) Color(0xFF00E5FF) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(p.clanTag, color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("K/D: ${p.kdRatio} • Фрагов: ${p.kills}", color = Color.Gray, fontSize = 10.sp)
                            }
                        }

                        // ELO rating
                        Text(
                            text = "${p.elo} ELO",
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumCard(player: LeaderboardPlayer, medal: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, color),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(medal, fontSize = 24.sp)
            Text(player.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
            Text(player.clanTag, color = Color(0xFF00E5FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text("${player.elo}", color = color, fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
    }
}
