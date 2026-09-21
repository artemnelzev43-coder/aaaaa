package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import android.content.Intent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.GameViewModel

@Composable
fun SettingsReferralScreen(viewModel: GameViewModel) {
    val profile by viewModel.playerProfile.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val banTicker by viewModel.banTicker.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var referralInput by remember { mutableStateOf("") }
    var referralFeedback by remember { mutableStateOf<String?>(null) }
    var pushNotificationsEnabled by remember { mutableStateOf(true) }
    var micSensitivity by remember { mutableFloatStateOf(75f) }
    var reportPlayerName by remember { mutableStateOf("") }
    var reportFeedback by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A0F))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text("НАСТРОЙКИ И РЕФЕРАЛЫ", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("Управление реферальной программой, защитой и звуком", color = Color(0xFF94A3B8), fontSize = 12.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Referral System Card (+50 coins per friend)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1B2A)),
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
                    Column {
                        Text("РЕФЕРАЛЬНАЯ СИСТЕМА & ПОДАРКИ", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("+500 монет другу по ссылке duckdns!", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text("🎁", fontSize = 26.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "🌐 При переходе по ссылке https://giveitem.duckdns.org/КОД открывается игра и дарит +500 монет. При повторном переходе появится окно с предупреждением, что ссылка уже использована.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stats row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Приглашено друзей: ${profile.invitedCount}", color = Color.White, fontSize = 13.sp)
                    Text("Баланс: ${profile.coins} 🪙", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Unique Code Box
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Ваша DuckDNS ссылка:", color = Color.Gray, fontSize = 10.sp)
                            Text("giveitem.duckdns.org/${profile.referralCode}", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row {
                            IconButton(
                                onClick = {
                                    val shareText = viewModel.getShareInviteText(profile.referralCode)
                                    clipboardManager.setText(AnnotatedString(shareText))
                                    referralFeedback = "Ссылка https://giveitem.duckdns.org/${profile.referralCode} скопирована!"
                                },
                                modifier = Modifier.size(36.dp).background(Color(0xFF334155), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Копировать", tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    val shareText = viewModel.getShareInviteText(profile.referralCode)
                                    clipboardManager.setText(AnnotatedString(shareText))
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Пригласить друга в Vanguard Ops")
                                    context.startActivity(shareIntent)
                                    referralFeedback = "Ссылка скопирована и отправлена!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("copy_invite_link_button")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Поделиться", tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ПРИГЛАСИТЬ", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Copy DuckDNS Server HTML Code button
                Button(
                    onClick = {
                        val serverHtml = """
<!DOCTYPE html>
<html lang="ru">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Vanguard Ops - Подарок 500 Монет</title>
  <style>
    body { background: #0b0f19; color: #fff; font-family: sans-serif; display: flex; align-items: center; justify-content: center; min-height: 100vh; margin: 0; padding: 20px; box-sizing: border-box; }
    .card { background: #151d2f; border: 2px solid #ffd700; border-radius: 20px; padding: 30px; text-align: center; max-width: 400px; width: 100%; box-shadow: 0 0 30px rgba(255,215,0,0.3); }
    h1 { color: #ffd700; font-size: 22px; margin-bottom: 10px; }
    p { color: #94a3b8; font-size: 14px; margin-bottom: 25px; line-height: 1.4; }
    .btn { display: block; background: #ffd700; color: #0b0f19; font-size: 16px; font-weight: bold; padding: 16px; border-radius: 12px; text-decoration: none; box-shadow: 0 4px 15px rgba(255,215,0,0.4); }
  </style>
</head>
<body>
  <div class="card">
    <div style="font-size: 50px; margin-bottom: 10px;">🎁</div>
    <h1>ПОДАРОК В VANGUARD OPS</h1>
    <p>Вам отправлен подарок в виде <strong>500 золотых монет</strong>! Нажмите кнопку ниже, чтобы открыть игру и забрать награду.</p>
    <a href="intent://giveitem.duckdns.org/${profile.referralCode}#Intent;scheme=https;package=com.aistudio.vanguardops.kxzq;end" class="btn" onclick="openApp()">🎁 ПОЛУЧИТЬ 500 МОНЕТ</a>
  </div>
  <script>
    function openApp() {
      setTimeout(function() {
        window.location.href = "vanguard://giveitem/${profile.referralCode}";
      }, 500);
    }
  </script>
</body>
</html>
                        """.trimIndent()
                        clipboardManager.setText(AnnotatedString(serverHtml))
                        referralFeedback = "HTML код для веб-страницы duckdns скопирован в буфер обмена!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(38.dp).testTag("copy_html_code_button")
                ) {
                    Text("📋 Скопировать HTML для сервера DuckDNS", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Enter friend code
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = referralInput,
                        onValueChange = { referralInput = it },
                        placeholder = { Text("Ввести код друга...", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (referralInput.isNotBlank()) {
                                viewModel.applyReferralCode(referralInput.trim()) { success, msg ->
                                    referralFeedback = msg
                                    if (success) {
                                        referralInput = ""
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(50.dp).testTag("submit_referral_code_button")
                    ) {
                        Text("ВВЕСТИ", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }

                referralFeedback?.let { msg ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(msg, color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Anti-Cheat Sentinel Protection & Report Form
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
                        Icon(Icons.Default.Security, contentDescription = "Античит", tint = Color(0xFF00E676))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("VANGUARD SENTINEL АНТИЧИТ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Surface(
                        color = Color(0xFF14532D),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("АКТИВЕН", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("• Защита от Aimbot, Wallhack и модификаций памяти", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Text("• Эвристический анализ траекторий пуль и скорострельности", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Text("• Защита от спидхака и рассинхронизации пакетов", color = Color(0xFF94A3B8), fontSize = 11.sp)

                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF080D16),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(banTicker, color = Color(0xFFFF5252), fontSize = 10.sp, modifier = Modifier.padding(8.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Report form
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = reportPlayerName,
                        onValueChange = { reportPlayerName = it },
                        placeholder = { Text("Никнейм подозрительного игрока...", color = Color.Gray, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF5252),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (reportPlayerName.isNotBlank()) {
                                viewModel.reportPlayer(reportPlayerName, "Подозрение в Aimbot/Wallhack")
                                reportFeedback = "Жалоба на $reportPlayerName отправлена в Sentinel!"
                                reportPlayerName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("ПОЖАЛОВАТЬСЯ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                reportFeedback?.let { fb ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(fb, color = Color(0xFF00E676), fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Push Notifications & Dark Theme Toggles
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Push Notifications
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = "Пуши", tint = Color(0xFF00E5FF))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Пуш-уведомления", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Турниры кланов, награды, испытания", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                    Switch(
                        checked = pushNotificationsEnabled,
                        onCheckedChange = { pushNotificationsEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E5FF))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dark Theme Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = "Тёмная тема", tint = Color(0xFF00E5FF))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Тёмная тактическая тема", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("OLED-контраст и энергосбережение", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { viewModel.toggleTheme() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E5FF))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Voice Chat Mic Sensitivity Slider
                Text("Чувствительность микрофона (Голосовой чат): ${micSensitivity.toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = micSensitivity,
                    onValueChange = { micSensitivity = it },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF00E5FF), activeTrackColor = Color(0xFF00E5FF))
                )
            }
        }
    }
}
