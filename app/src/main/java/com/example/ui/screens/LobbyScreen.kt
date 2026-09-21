package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@Composable
fun LobbyScreen(viewModel: GameViewModel) {
    val profile by viewModel.playerProfile.collectAsState()
    val challenges by viewModel.challenges.collectAsState()
    val dailyRewards by viewModel.dailyRewards.collectAsState()
    val antiCheat by viewModel.antiCheatStatus.collectAsState()
    val lastSync by viewModel.lastCloudSyncText.collectAsState()

    val nextLevelXp = profile.level * 500
    val xpFraction = (profile.xp.toFloat() / nextLevelXp.toFloat()).coerceIn(0f, 1f)
    val todayReward = dailyRewards.find { it.isAvailableToday }
    var showMultiplayerDialog by remember { mutableStateOf(false) }

    if (showMultiplayerDialog) {
        MultiplayerRoomDialog(
            viewModel = viewModel,
            onDismiss = { showMultiplayerDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A0F))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 1. Top Operative Header & Balances
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Level Avatar Shield
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    brush = Brush.radialGradient(listOf(Color(0xFF00E5FF), Color(0xFF0D47A1))),
                                    shape = CircleShape
                                )
                                .border(2.dp, Color(0xFF80D8FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${profile.level}",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFF1E293B),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = profile.clanTag,
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Ранг: Спецназ Авангарда • Уровень ${profile.level}",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Coins Balance Pill
                    Surface(
                        color = Color(0xFF14532D),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💰", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${profile.coins}",
                                color = Color(0xFFE8F5E9),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // XP Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Боевой опыт (XP)", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text("${profile.xp} / $nextLevelXp XP", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { xpFraction },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF00E5FF),
                    trackColor = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Firebase & Play Games indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDone, contentDescription = "Firebase Sync", tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Облако Firebase: Синхронизировано", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SportsEsports, contentDescription = "Google Play", tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Google Play Games", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Play Actions (Match + Multiplayer Room)
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { viewModel.navigateTo(AppScreen.MATCH) },
                modifier = Modifier
                    .weight(1.3f)
                    .height(68.dp)
                    .testTag("start_match_primary_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF8A80))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "В бой", modifier = Modifier.size(32.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("В БОЙ", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Text("Одиночный матч", color = Color(0xFFFFCDD2), fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = { showMultiplayerDialog = true },
                modifier = Modifier
                    .weight(1.3f)
                    .height(68.dp)
                    .testTag("open_multiplayer_dialog_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF)),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF80D8FF))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wifi, contentDescription = "Мультиплеер", modifier = Modifier.size(28.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("МУЛЬТИПЛЕЕР", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("Комнаты • Wi-Fi", color = Color(0xFF0D47A1), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Daily Reward Banner (if available)
        todayReward?.let { reward ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF818CF8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎁", fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Ежедневная награда (День ${reward.day})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("+${reward.coins} монет • +${reward.xp} XP", color = Color(0xFF818CF8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Button(
                        onClick = { viewModel.claimDailyReward(reward) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("claim_daily_reward_button")
                    ) {
                        Text("ЗАБРАТЬ", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. Referral Invite Banner (+50 Coins)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0E253A)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateTo(AppScreen.SETTINGS) }
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = "Рефералы", tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Пригласи друга — получи 50 монет!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Приглашено друзей: ${profile.invitedCount} • Заработано: ${profile.invitedCount * 50} монет", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }
                Text("🔗", fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Tactical Stats Overview Card
        Text("БОЕВАЯ СТАТИСТИКА", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatBox(title = "ПОБЕДЫ", value = "${profile.wins}", subtitle = "Поражений: ${profile.losses}", modifier = Modifier.weight(1f))
            StatBox(title = "ФРАГИ", value = "${profile.kills}", subtitle = "Смертей: ${profile.deaths}", modifier = Modifier.weight(1f))
            StatBox(title = "ХЕДШОТЫ", value = "${profile.headshots}", subtitle = "В голову", modifier = Modifier.weight(1f))
            StatBox(title = "РАЗРУШЕНО", value = "${profile.cratesDestroyed}", subtitle = "Укрытий", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Active Weekly Challenge Widget
        val activeChallenge = challenges.firstOrNull { !it.isClaimed }
        activeChallenge?.let { chal ->
            Text("АКТИВНОЕ ИСПЫТАНИЕ НЕДЕЛИ", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(chal.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("+${chal.coinReward} $ • +${chal.xpReward} XP", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text(chal.description, color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { chal.progressFraction },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF00E676),
                        trackColor = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${chal.current} / ${chal.goal}", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.align(Alignment.End))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Anti-Cheat Status Ticker
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF090D14)),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Security, contentDescription = "Античит", tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(antiCheat, color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = Color.Gray, fontSize = 9.sp)
        }
    }
}
