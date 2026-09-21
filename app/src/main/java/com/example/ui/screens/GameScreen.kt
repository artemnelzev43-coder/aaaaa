package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Weapon
import com.example.data.model.WeaponCategory
import com.example.game.TacticalRaycastCanvas
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val game = viewModel.gameEngine
    val selectedWeapon by viewModel.selectedWeapon.collectAsState()
    val ownedSkins by viewModel.ownedSkins.collectAsState()
    val isPttActive by viewModel.isVoicePttActive.collectAsState()
    val voiceDb by viewModel.voiceDecibels.collectAsState()
    val radioMsg by viewModel.activeRadioCallout.collectAsState()
    val isReceivingVoice by viewModel.isVoiceReceiving.collectAsState()
    val speakerName by viewModel.activeSpeakerName.collectAsState()

    var showBuyMenu by remember { mutableStateOf(false) }
    var showRadioMenu by remember { mutableStateOf(false) }
    var showReportMenu by remember { mutableStateOf(false) }

    val currentSkin = remember(selectedWeapon, ownedSkins) {
        val equippedEntity = ownedSkins.find { it.weaponId == selectedWeapon.id && it.isEquipped }
        viewModel.repository.allSkins.find { it.id == equippedEntity?.skinId }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF070A0F))) {
        // 1. Raycast 3D Canvas
        TacticalRaycastCanvas(
            game = game,
            currentSkin = currentSkin,
            modifier = Modifier.fillMaxSize()
        )

        // Incoming Real Voice Chat Indicator
        AnimatedVisibility(
            visible = isReceivingVoice,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp)
        ) {
            Surface(
                color = Color(0xDD00E676),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔊", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ГОЛОС: $speakerName в эфире",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 2. Gesture Aiming Surface (Right and upper viewport)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp) // Leave lower area clear for controls
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val sensitivity = if (game.isAds) 0.004f else 0.007f
                        game.rotatePlayer(dragAmount.x * sensitivity)
                    }
                }
        )

        // 3. Top Tactical Match HUD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit button & Minimap buffer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.LOBBY) },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xAA111827), CircleShape)
                            .border(1.dp, Color(0xFF00E5FF), CircleShape)
                            .testTag("exit_match_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "В лобби", tint = Color(0xFF00E5FF))
                    }
                    Spacer(modifier = Modifier.width(68.dp)) // Minimap offset

                    // Team Score Banner
                    Surface(
                        color = Color(0xCC0B101B),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2937))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("DELTA", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${game.teamScoreDelta}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text(" : ", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${game.teamScoreSpectre}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SPECTRE", color = Color(0xFFFF1744), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Balance & Armory Buy Menu Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xDD1B5E20),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676))
                    ) {
                        Text(
                            text = "$ ${game.roundCredits}",
                            color = Color(0xFFE8F5E9),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { showBuyMenu = true },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF00E5FF), RoundedCornerShape(8.dp))
                            .testTag("open_buy_menu_button")
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Закупка оружия", tint = Color.Black)
                    }
                }
            }

            // Killfeed list (Top Right)
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                Column(horizontalAlignment = Alignment.End) {
                    game.killFeed.takeLast(3).forEach { feedItem ->
                        Surface(
                            color = Color(0xCC0F172A),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = feedItem,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Radio Comms Banner (if active)
        radioMsg?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(0xEE0D47A1),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29B6F6))
                ) {
                    Text(
                        text = "🔊 [Рация]: $msg",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 5. Bottom HUD & Interactive Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            // Health & Armor Bar + Ammo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // HP & Armor
                Column(modifier = Modifier.width(160.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("HP", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        LinearProgressIndicator(
                            progress = { (game.playerHp / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF00E676),
                            trackColor = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${game.playerHp}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("AP", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        LinearProgressIndicator(
                            progress = { (game.playerArmor / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF00E5FF),
                            trackColor = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${game.playerArmor}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Weapon & Ammo Display
                Surface(
                    color = Color(0xCC0F172A),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedWeapon.name,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            currentSkin?.let {
                                Text(
                                    text = it.name,
                                    color = it.rarity.color,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "${game.currentMagAmmo}",
                            color = if (game.currentMagAmmo <= 5) Color(0xFFFF1744) else Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        )
                        Text(
                            text = " / ${game.currentReserveAmmo}",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Control Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Movement Virtual D-Pad (Left side)
                VirtualMovementPad(game = game)

                // Tactical Actions Center: Voice Chat & Radio Comms
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Voice Chat PTT Button
                    Button(
                        onClick = { viewModel.setVoicePtt(!isPttActive) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPttActive) Color(0xFF00E676) else Color(0xAA1E293B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isPttActive) Color.White else Color(0xFF00E5FF)
                        ),
                        modifier = Modifier.testTag("ptt_voice_button")
                    ) {
                        Icon(
                            imageVector = if (isPttActive) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Голосовой чат",
                            tint = if (isPttActive) Color.Black else Color(0xFF00E5FF)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPttActive) "В ЭФИРЕ ($voiceDb dB)" else "РАЦИЯ (PTT)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPttActive) Color.Black else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Radio quick commands
                    OutlinedButton(
                        onClick = { showRadioMenu = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp).testTag("radio_commands_button")
                    ) {
                        Text("КОМАНДЫ РАЦИИ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Combat Action Buttons (Right side: Fire, ADS, Reload, Grenade)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Grenade Button
                    IconButton(
                        onClick = { viewModel.throwGrenade() },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFE65100), CircleShape)
                            .border(1.dp, Color(0xFFFF9100), CircleShape)
                            .testTag("throw_grenade_button")
                    ) {
                        Text("💣", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Reload Button
                    IconButton(
                        onClick = { viewModel.reload() },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xCC1E293B), CircleShape)
                            .border(1.dp, Color(0xFF64748B), CircleShape)
                            .testTag("reload_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Перезарядка", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // ADS Zoom Button
                    IconButton(
                        onClick = { viewModel.toggleAds() },
                        modifier = Modifier
                            .size(46.dp)
                            .background(if (game.isAds) Color(0xFF00E5FF) else Color(0xCC1E293B), CircleShape)
                            .border(1.5.dp, Color(0xFF00E5FF), CircleShape)
                            .testTag("ads_zoom_button")
                    ) {
                        Icon(
                            Icons.Default.CrisisAlert,
                            contentDescription = "Прицел",
                            tint = if (game.isAds) Color.Black else Color(0xFF00E5FF)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // FIRE Button (Primary big action button)
                    Button(
                        onClick = { viewModel.fireWeapon() },
                        modifier = Modifier
                            .size(76.dp)
                            .testTag("primary_fire_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                        border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFFF8A80))
                    ) {
                        Text("ОГОНЬ", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        // 6. Anti-Cheat Sentinel Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = "Античит", tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sentinel Anti-Cheat: OK", color = Color(0xFF94A3B8), fontSize = 10.sp)
            }
        }

        // Buy Menu Dialog
        if (showBuyMenu) {
            BuyMenuDialog(
                viewModel = viewModel,
                onDismiss = { showBuyMenu = false }
            )
        }

        // Radio Commands Dialog
        if (showRadioMenu) {
            RadioCommandsDialog(
                onSelectCallout = { msg ->
                    viewModel.sendRadioCallout(msg)
                    showRadioMenu = false
                },
                onDismiss = { showRadioMenu = false }
            )
        }
    }
}

@Composable
fun VirtualMovementPad(game: com.example.game.TacticalGameEngine) {
    var stickOffsetX by remember { mutableFloatStateOf(0f) }
    var stickOffsetY by remember { mutableFloatStateOf(0f) }
    val maxRadiusPx = 100f // max deflection

    Surface(
        color = Color(0x990A111C),
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E5FF)),
        modifier = Modifier
            .size(130.dp)
            .testTag("virtual_joystick_pad")
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val dx = (offset.x - centerX).coerceIn(-maxRadiusPx, maxRadiusPx)
                        val dy = (offset.y - centerY).coerceIn(-maxRadiusPx, maxRadiusPx)
                        stickOffsetX = dx
                        stickOffsetY = dy
                        game.inputForward = (-dy / maxRadiusPx).coerceIn(-1f, 1f)
                        game.inputStrafe = (dx / maxRadiusPx).coerceIn(-1f, 1f)
                        game.renderTick++
                    },
                    onDragEnd = {
                        stickOffsetX = 0f
                        stickOffsetY = 0f
                        game.inputForward = 0f
                        game.inputStrafe = 0f
                        game.renderTick++
                    },
                    onDragCancel = {
                        stickOffsetX = 0f
                        stickOffsetY = 0f
                        game.inputForward = 0f
                        game.inputStrafe = 0f
                        game.renderTick++
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newX = (stickOffsetX + dragAmount.x).coerceIn(-maxRadiusPx, maxRadiusPx)
                        val newY = (stickOffsetY + dragAmount.y).coerceIn(-maxRadiusPx, maxRadiusPx)
                        stickOffsetX = newX
                        stickOffsetY = newY
                        // Map stick to forward / strafe
                        game.inputForward = (-newY / maxRadiusPx).coerceIn(-1f, 1f)
                        game.inputStrafe = (newX / maxRadiusPx).coerceIn(-1f, 1f)
                        game.renderTick++
                    }
                )
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Directional indicators with direct tap buttons
            IconButton(
                onClick = { game.stepForward() },
                modifier = Modifier.align(Alignment.TopCenter).size(34.dp).testTag("step_forward_button")
            ) {
                Text("▲", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
            IconButton(
                onClick = { game.stepBackward() },
                modifier = Modifier.align(Alignment.BottomCenter).size(34.dp).testTag("step_backward_button")
            ) {
                Text("▼", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
            IconButton(
                onClick = { game.stepStrafeLeft() },
                modifier = Modifier.align(Alignment.CenterStart).size(34.dp).testTag("step_left_button")
            ) {
                Text("◀", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
            IconButton(
                onClick = { game.stepStrafeRight() },
                modifier = Modifier.align(Alignment.CenterEnd).size(34.dp).testTag("step_right_button")
            ) {
                Text("▶", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 14.sp)
            }

            // Draggable analog knob
            Box(
                modifier = Modifier
                    .offset { IntOffset(stickOffsetX.toInt(), stickOffsetY.toInt()) }
                    .size(46.dp)
                    .background(Color(0xFF00E5FF), CircleShape)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFF070A0F), CircleShape)
                )
            }
        }
    }
}

@Composable
fun BuyMenuDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(WeaponCategory.RIFLE) }
    val filteredWeapons = remember(selectedCategory) {
        viewModel.repository.allWeapons.filter { it.category == selectedCategory }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().height(520.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D131F)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "АРСЕНАЛ ЗАКУПКИ",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Баланс раунда: $ ${viewModel.gameEngine.roundCredits}",
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("ЗАКРЫТЬ", fontSize = 11.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Tabs
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    WeaponCategory.entries.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        Surface(
                            onClick = { selectedCategory = cat },
                            color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = cat.displayName,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Weapons in category
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredWeapons) { weapon ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3E5C))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(weapon.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Text(
                                        "Урон: ${weapon.damage} | Магазин: ${weapon.magSize} | Скорострельность: ${weapon.fireRateMs}мс",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                    Text(weapon.description, color = Color(0xFF94A3B8), fontSize = 10.sp, maxLines = 1)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Button(
                                    onClick = {
                                        val success = viewModel.buyItem(weapon)
                                        if (success) onDismiss()
                                    },
                                    enabled = viewModel.gameEngine.roundCredits >= weapon.price,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00E5FF),
                                        disabledContainerColor = Color(0xFF334155)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "$ ${weapon.price}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = if (viewModel.gameEngine.roundCredits >= weapon.price) Color.Black else Color.Gray
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

@Composable
fun RadioCommandsDialog(
    onSelectCallout: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val callouts = listOf(
        "Враг замечен на точке A!",
        "Враг замечен на точке B!",
        "Нужно подкрепление!",
        "Перезаряжаюсь, прикройте!",
        "Точка зачищена, путь свободен!",
        "Отступаем на базу!",
        "Граната пошла!",
        "Держим позиции!"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "БЫСТРЫЕ КОМАНДЫ РАЦИИ",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                callouts.forEach { callout ->
                    Surface(
                        onClick = { onSelectCallout(callout) },
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "📻 $callout",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}
