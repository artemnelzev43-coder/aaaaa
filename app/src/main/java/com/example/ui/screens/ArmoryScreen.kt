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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.Weapon
import com.example.data.model.WeaponCategory
import com.example.data.model.WeaponSkin
import com.example.ui.viewmodel.GameViewModel

@Composable
fun ArmoryScreen(viewModel: GameViewModel) {
    val selectedWeapon by viewModel.selectedWeapon.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()
    val ownedSkins by viewModel.ownedSkins.collectAsState()

    var activeCategory by remember { mutableStateOf(selectedWeapon.category) }
    val weaponsInCategory = remember(activeCategory) {
        viewModel.repository.allWeapons.filter { it.category == activeCategory }
    }

    val skinsForSelectedWeapon = remember(selectedWeapon) {
        viewModel.repository.allSkins.filter { it.weaponId == selectedWeapon.id }
    }

    val equippedSkinId = remember(selectedWeapon, ownedSkins) {
        ownedSkins.find { it.weaponId == selectedWeapon.id && it.isEquipped }?.skinId
            ?: skinsForSelectedWeapon.firstOrNull()?.id
    }

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
                Text("АРСЕНАЛ И СКИНЫ", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text("Уникальные скины и кастомизация оружия", color = Color(0xFF94A3B8), fontSize = 12.sp)
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

        Spacer(modifier = Modifier.height(14.dp))

        // Category Pills
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(WeaponCategory.entries) { cat ->
                val isSelected = cat == activeCategory
                Surface(
                    onClick = { activeCategory = cat },
                    color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = cat.displayName,
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Weapon Selector Horizontal Carousel
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(weaponsInCategory) { weapon ->
                val isSelected = weapon.id == selectedWeapon.id
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF16253B) else Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier
                        .clickable { viewModel.selectWeapon(weapon) }
                        .width(140.dp)
                        .testTag("weapon_select_${weapon.id}")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(weapon.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp, maxLines = 1)
                        Text("$ ${weapon.price}", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Weapon Showcase Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1420)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val currentEquippedSkin = skinsForSelectedWeapon.find { it.id == equippedSkinId }
                val skinGradient = currentEquippedSkin?.let {
                    Brush.horizontalGradient(listOf(Color(it.primaryColor), Color(it.accentColor)))
                } ?: Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF334155)))

                // Weapon Skin Visual Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(skinGradient)
                        .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🔫 ${selectedWeapon.name}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        currentEquippedSkin?.let {
                            Surface(
                                color = Color(0x99000000),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "${it.rarity.title} • ${it.name}",
                                    color = it.rarity.color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Sliders
                StatRow("Урон (Damage)", selectedWeapon.damage.toFloat(), 120f, Color(0xFFFF1744))
                StatRow("Скорострельность", (1000f - selectedWeapon.fireRateMs).coerceAtLeast(0f), 1000f, Color(0xFFFF9100))
                StatRow("Дальность (Range)", selectedWeapon.range, 150f, Color(0xFF00E5FF))
                StatRow("Ёмкость магазина", selectedWeapon.magSize.toFloat(), 50f, Color(0xFF00E676))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Unique Skins Roster for this weapon
        Text("ДОСТУПНЫЕ СКИНЫ", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(skinsForSelectedWeapon) { skin ->
                val isOwned = ownedSkins.any { it.skinId == skin.id } || skin.isOwned
                val isEquipped = equippedSkinId == skin.id

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEquipped) Color(0xFF132338) else Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isEquipped) 2.dp else 1.dp,
                        if (isEquipped) Color(0xFF00E5FF) else skin.rarity.color.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Color Swatch
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.linearGradient(listOf(Color(skin.primaryColor), Color(skin.accentColor)))
                                    )
                                    .border(1.dp, skin.rarity.color, RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(skin.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(skin.rarity.title, color = skin.rarity.color, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                            }
                        }

                        // Action Button
                        if (isEquipped) {
                            Surface(
                                color = Color(0xFF00E5FF),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "В БОЮ",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        } else if (isOwned) {
                            Button(
                                onClick = { viewModel.equipSkin(skin.weaponId, skin.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF))
                            ) {
                                Text("НАДЕТЬ", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "В КЕЙСАХ 🔒",
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: Float, maxVal: Float, color: Color) {
    val fraction = (value / maxVal).coerceIn(0f, 1f)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFF94A3B8), fontSize = 11.sp, modifier = Modifier.width(130.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("${value.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(28.dp))
    }
}
