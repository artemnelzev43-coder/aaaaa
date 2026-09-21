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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Clan
import com.example.ui.viewmodel.GameViewModel

@Composable
fun ClansScreen(viewModel: GameViewModel) {
    val clans by viewModel.clans.collectAsState()
    val members = viewModel.repository.clanMembers
    val myClan = clans.find { it.isUserMember } ?: clans.first()

    var showJoinFeedback by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A0F))
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("СИСТЕМА КЛАНОВ", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text("Кроссплатформенный мультиплеер и кланвары", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "ТОП-1 КЛАН",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // My Clan Hero Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.radialGradient(listOf(Color(0xFF00E5FF), Color(0xFF0B192C))))
                                .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛡️", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(myClan.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(myClan.tag, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text(
                                "Уровень клана ${myClan.level} • Участников: ${myClan.membersCount}/${myClan.maxMembers}",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Rating Pill
                    Surface(
                        color = Color(0xFF1E1B4B),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF818CF8))
                    ) {
                        Text(
                            text = "Рейтинг: ${myClan.clanRating}",
                            color = Color(0xFFC7D2FE),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(myClan.description, color = Color(0xFFCBD5E1), fontSize = 11.sp)

                Spacer(modifier = Modifier.height(12.dp))

                // Clan action buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showJoinFeedback = "Вы внесли 100 очков опыта в кланвар!" },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("contribute_clan_button")
                    ) {
                        Text("ВНЕСТИ ВКЛАД", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { showJoinFeedback = "Поиск кланвара запущен! Подбор оппонентов..." },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.weight(1f).testTag("clan_war_matchmaking_button")
                    ) {
                        Text("КЛАНВАР 5v5", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                showJoinFeedback?.let { feedback ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(feedback, color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Members Roster (Cross-Platform)
        Text("СОСТАВ ОТРЯДА (КРОССПЛАТФОРМЕННЫЙ)", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(members) { member ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Online indicator dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (member.isOnline) Color(0xFF00E676) else Color.Gray, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Platform Icon
                            Text(member.platform.iconText, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(member.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${member.role} • ${member.platform.displayName}", color = Color.Gray, fontSize = 10.sp)
                            }
                        }

                        // Score
                        Text(
                            text = "${member.score} PTS",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
