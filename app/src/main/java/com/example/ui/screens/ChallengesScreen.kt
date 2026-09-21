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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.GameViewModel

@Composable
fun ChallengesScreen(viewModel: GameViewModel) {
    val challenges by viewModel.challenges.collectAsState()

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
                Text("ЕЖЕНЕДЕЛЬНЫЕ ИСПЫТАНИЯ", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Выполняй боевые задания и получай монеты и опыт", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Timer, contentDescription = "Таймер", tint = Color(0xFF00E5FF), modifier = Modifier.padding(end = 4.dp))
                    Text("4 дня", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(challenges) { chal ->
                val isCompleted = chal.isCompleted
                val isClaimed = chal.isClaimed

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isClaimed) Color(0xFF0A0E17) else Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isCompleted && !isClaimed) Color(0xFF00E676) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(chal.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "+${chal.coinReward} монет • +${chal.xpReward} XP",
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(chal.description, color = Color(0xFF94A3B8), fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar & Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                LinearProgressIndicator(
                                    progress = { chal.progressFraction },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = if (isCompleted) Color(0xFF00E676) else Color(0xFF00E5FF),
                                    trackColor = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${chal.current} / ${chal.goal}",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }

                            if (isClaimed) {
                                Surface(
                                    color = Color(0xFF142018),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Получено", tint = Color(0xFF00E676))
                                        Text("ПОЛУЧЕНО", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else if (isCompleted) {
                                Button(
                                    onClick = { viewModel.claimChallenge(chal) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("claim_challenge_${chal.id}")
                                ) {
                                    Text("ЗАБРАТЬ", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                            } else {
                                Surface(
                                    color = Color(0xFF1E293B),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        "В ПРОЦЕССЕ",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
