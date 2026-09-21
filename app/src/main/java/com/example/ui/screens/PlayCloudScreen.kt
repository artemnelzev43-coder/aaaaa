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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
fun PlayCloudScreen(viewModel: GameViewModel) {
    val achievements = viewModel.repository.playAchievements
    val isSyncing by viewModel.cloudSyncing.collectAsState()
    val lastSyncText by viewModel.lastCloudSyncText.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()
    val isGoogleLoading by viewModel.isGoogleSignInLoading.collectAsState()
    val googleStatus by viewModel.googleAuthStatus.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

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
                Text("GOOGLE PLAY & FIREBASE", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Вход через Google, Play Games и Cloud Save", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
            Icon(Icons.Default.SportsEsports, contentDescription = "Play Games", tint = Color(0xFF00E676), modifier = Modifier.size(28.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Google Sign-In / Account Link Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (profile.isGoogleLinked) Color(0xFF0C2A1E) else Color(0xFF1E1E2E)
            ),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (profile.isGoogleLinked) Color(0xFF00E676) else Color(0xFF818CF8)
            ),
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
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (profile.isGoogleLinked) Color(0xFF00C853) else Color(0xFF4F46E5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Google Account", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (profile.isGoogleLinked) "Google аккаунт привязан" else "Вход с помощью Google",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (profile.isGoogleLinked) "ID: ${profile.googlePlayerId ?: "Синхронизирован"} • ${profile.email.ifBlank { "simalpoal@gmail.com" }}" else "Google Play Games Services ID",
                                color = if (profile.isGoogleLinked) Color(0xFF69F0AE) else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                googleStatus?.let { status ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(status, color = Color(0xFF80D8FF), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (profile.isGoogleLinked) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = { viewModel.signOutGoogle() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp).testTag("google_sign_out_button")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = "Выйти", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ВЫЙТИ ИЗ АККАУНТА", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = { viewModel.signInWithGoogle(activity) },
                        enabled = !isGoogleLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("google_sign_in_button")
                    ) {
                        if (isGoogleLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ПОДКЛЮЧЕНИЕ К GOOGLE...", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        } else {
                            Text("ВОЙТИ С ПОМОЩЬЮ GOOGLE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Firebase Cloud Save Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B2B)),
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
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = "Облако", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Firebase Firestore Cloud Save", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Синхронизация данных: АКТИВНА", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Последняя синхронизация: $lastSyncText",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Text(
                    text = "Сохраняются: ${profile.coins} монет, уровень ${profile.level}, скины оружия, клановый прогресс.",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.syncWithFirebase() },
                    enabled = !isSyncing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("sync_firebase_button")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("СИНХРОНИЗАЦИЯ...", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.CloudSync, contentDescription = "Синхронизировать", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("СИНХРОНИЗИРОВАТЬ С ОБЛАКОМ", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google Play Games Achievements Header
        Text("ДОСТИЖЕНИЯ GOOGLE PLAY ИГРЫ", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(achievements) { ach ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (ach.isUnlocked) Color(0xFF0F2318) else Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (ach.isUnlocked) Color(0xFF00E676) else Color(0xFF1E293B)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(ach.iconEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(ach.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(ach.description, color = Color(0xFF94A3B8), fontSize = 11.sp)
                                if (!ach.isUnlocked) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Прогресс: ${ach.progress} / ${ach.maxProgress}",
                                        color = Color(0xFF00E5FF),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Badge / Status
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                color = if (ach.isUnlocked) Color(0xFF00E676) else Color(0xFF1E293B),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (ach.isUnlocked) "РАЗБЛОКИРОВАНО" else "+${ach.xpValue} XP",
                                    color = if (ach.isUnlocked) Color.Black else Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
