package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WeaponCase
import com.example.data.model.WeaponSkin
import com.example.ui.viewmodel.CaseSpinState
import com.example.ui.viewmodel.GameViewModel
import kotlin.math.roundToInt

@Composable
fun CasesScreen(viewModel: GameViewModel) {
    val cases = viewModel.repository.cases
    val profile by viewModel.playerProfile.collectAsState()
    val caseState by viewModel.caseState.collectAsState()

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
                Text("ОРУЖЕЙНЫЕ КЕЙСЫ", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text("Открывай кейсы за заработанные монеты", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
            Surface(
                color = Color(0xFF14532D),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676))
            ) {
                Text(
                    text = "💰 ${profile.coins}",
                    color = Color(0xFFE8F5E9),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cases List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(cases) { c ->
                CaseCard(
                    case = c,
                    canAfford = profile.coins >= c.price,
                    onOpen = { viewModel.openCase(c) }
                )
            }
        }
    }

    // Interactive Spinning Roulette Dialog
    when (val state = caseState) {
        is CaseSpinState.Spinning -> {
            SpinningRouletteDialog(candidateSkins = state.candidateSkins, targetSkin = state.targetSkin)
        }
        is CaseSpinState.Revealed -> {
            WonSkinDialog(wonSkin = state.wonSkin, onClaim = { viewModel.dismissCaseResult() })
        }
        CaseSpinState.Idle -> {}
    }
}

@Composable
fun CaseCard(
    case: WeaponCase,
    canAfford: Boolean,
    onOpen: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(case.colorHex)),
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
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.radialGradient(listOf(Color(case.colorHex), Color(0xFF0B0F19)))
                            )
                            .border(1.dp, Color(case.colorHex), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📦", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(case.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(case.description, color = Color(0xFF94A3B8), fontSize = 11.sp, maxLines = 2)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Стоимость: ${case.price} монет",
                        color = Color(0xFF00E676),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Button(
                    onClick = onOpen,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(case.colorHex),
                        disabledContainerColor = Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("open_case_${case.id}")
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = "Открыть", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (canAfford) "ОТКРЫТЬ" else "НЕДОСТАТОЧНО",
                        color = if (canAfford) Color.Black else Color.Gray,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SpinningRouletteDialog(
    candidateSkins: List<WeaponSkin>,
    targetSkin: WeaponSkin
) {
    var startSpin by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startSpin = true
    }

    // Target index is 25 (item in center)
    val itemWidthPx = 100f
    val targetOffset = -(25 * itemWidthPx)

    val animatedOffset by animateFloatAsState(
        targetValue = if (startSpin) targetOffset else 0f,
        animationSpec = tween(durationMillis = 3400, easing = FastOutSlowInEasing),
        label = "roulette_spin"
    )

    Dialog(onDismissRequest = {}) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF090D15)),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700)),
            modifier = Modifier.fillMaxWidth().height(260.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ВЫБОР СКИНА...",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Horizontal Spinning Carousel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(Color(0xFF030712))
                            .border(1.dp, Color(0xFF334155))
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Carousel items row
                        Row(
                            modifier = Modifier
                                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                                .padding(horizontal = 140.dp)
                        ) {
                            candidateSkins.forEach { skin ->
                                Box(
                                    modifier = Modifier
                                        .width(96.dp)
                                        .height(100.dp)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(skin.primaryColor))
                                        .border(2.dp, skin.rarity.color, RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Text("🔫", fontSize = 20.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(skin.name, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text(skin.rarity.title, color = skin.rarity.color, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        // Center Golden Crosshair Needle
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(110.dp)
                                .background(Color(0xFFFFD700))
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.TopCenter)
                                .background(Color(0xFFFFD700), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.BottomCenter)
                                .background(Color(0xFFFFD700), CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WonSkinDialog(
    wonSkin: WeaponSkin,
    onClaim: () -> Unit
) {
    Dialog(onDismissRequest = onClaim) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, wonSkin.rarity.color),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ПОЗДРАВЛЯЕМ!",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
                Text("Вы выбили новый скин из кейса:", color = Color(0xFF94A3B8), fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                // Big Skin Showcase Badge
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.radialGradient(listOf(wonSkin.rarity.color.copy(alpha = 0.8f), Color(0xFF0B101D)))
                        )
                        .border(2.dp, wonSkin.rarity.color, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔫", fontSize = 42.sp)
                        Text(
                            text = wonSkin.rarity.title,
                            color = wonSkin.rarity.color,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(wonSkin.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text(wonSkin.rarity.title, color = wonSkin.rarity.color, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("claim_won_skin_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Забрать", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ЗАБРАТЬ В АРСЕНАЛ", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}
