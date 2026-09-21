package com.example.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.data.model.WeaponSkin
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

data class RayHit(
    val dist: Float,
    val wallType: TileType,
    val side: Int, // 0 for vertical, 1 for horizontal
    val wallX: Float,
    val tileIndex: Int
)

@Composable
fun TacticalRaycastCanvas(
    game: TacticalGameEngine,
    currentSkin: WeaponSkin?,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val tick = game.renderTick // Trigger recomposition / draw on every engine tick

    Canvas(modifier = modifier.fillMaxSize()) {
        val _tick = tick // Ensure Canvas DrawScope observes state
        val w = size.width
        val h = size.height
        val halfH = h / 2f

        // 1. Sky & Ground Ceiling/Floor Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF07090E), Color(0xFF101726)),
                startY = 0f,
                endY = halfH
            ),
            size = Size(w, halfH)
        )
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF141923), Color(0xFF090B10)),
                startY = halfH,
                endY = h
            ),
            size = Size(w, halfH),
            topLeft = Offset(0f, halfH)
        )

        // Draw tactical ceiling/floor grid lines
        for (gridY in 1..4) {
            val yOffset = halfH + (gridY * gridY * 22f)
            if (yOffset < h) {
                drawLine(
                    color = Color(0x1A00E5FF),
                    start = Offset(0f, yOffset),
                    end = Offset(w, yOffset),
                    strokeWidth = 1f
                )
            }
        }

        // 2. Raycasting 3D Walls
        val numRays = (w / 4f).toInt().coerceIn(60, 160)
        val sliceWidth = w / numRays
        val fov = game.fov
        val halfFov = fov / 2f
        val depthBuffer = FloatArray(numRays)

        for (i in 0 until numRays) {
            val rayAngle = game.playerAngle - halfFov + (i.toFloat() / numRays) * fov
            val hit = castDdaRay(game, rayAngle)
            val correctedDist = max(0.1f, hit.dist * cos(rayAngle - game.playerAngle))
            depthBuffer[i] = correctedDist

            val wallHeight = (h / correctedDist).coerceIn(4f, h * 2.2f)
            val top = halfH - (wallHeight / 2f)

            // Dynamic distance shading & muzzle flash lighting
            val fogFactor = (1f - (correctedDist / 20f)).coerceIn(0.15f, 1.0f)
            val flashBonus = if (game.muzzleFlashTimer > 0) 0.35f else 0f
            val brightness = (fogFactor + flashBonus).coerceIn(0f, 1f)

            // Select wall color based on TileType
            val baseColor = when (hit.wallType) {
                TileType.BUNKER_WALL -> Color(0xFF37474F)
                TileType.CYBER_STEEL -> Color(0xFF1E293B)
                TileType.WOODEN_CRATE -> Color(0xFF8D6E63) // Wood brown
                TileType.EXPLOSIVE_BARREL -> Color(0xFFFF3D00) // Hazard red/orange
                TileType.SANDBAG_COVER -> Color(0xFF6D4C41)
                else -> Color(0xFF263238)
            }

            // Darken side 1 for 3D depth contrast
            val sideDim = if (hit.side == 1) 0.72f else 1.0f
            val finalWallColor = Color(
                red = (baseColor.red * brightness * sideDim).coerceIn(0f, 1f),
                green = (baseColor.green * brightness * sideDim).coerceIn(0f, 1f),
                blue = (baseColor.blue * brightness * sideDim).coerceIn(0f, 1f),
                alpha = 1.0f
            )

            drawRect(
                color = finalWallColor,
                topLeft = Offset(i * sliceWidth, top),
                size = Size(sliceWidth + 0.5f, wallHeight)
            )

            // Decorative wall features (Cyber lines, Crate damage cracks, Barrel hazard bands)
            if (hit.wallType == TileType.EXPLOSIVE_BARREL) {
                // Hazard yellow stripes on barrel
                val bandTop = top + wallHeight * 0.35f
                val bandH = wallHeight * 0.12f
                drawRect(
                    color = Color(0xFFFFEA00).copy(alpha = brightness),
                    topLeft = Offset(i * sliceWidth, bandTop),
                    size = Size(sliceWidth + 0.5f, bandH)
                )
            } else if (hit.wallType == TileType.WOODEN_CRATE) {
                // Check crate health
                val destructible = game.destructibles[hit.tileIndex]
                if (destructible != null && destructible.currentHp < destructible.type.maxHp) {
                    // Draw splinter cracks across crate
                    val crackAlpha = (1f - destructible.currentHp.toFloat() / destructible.type.maxHp).coerceIn(0.2f, 0.9f)
                    drawRect(
                        color = Color(0xFF2E1C0C).copy(alpha = crackAlpha),
                        topLeft = Offset(i * sliceWidth, top + wallHeight * 0.48f),
                        size = Size(sliceWidth + 0.5f, 2f)
                    )
                }
            } else if (hit.wallType == TileType.CYBER_STEEL) {
                // Neon seam
                if (hit.side == 0 && (hit.wallX in 0.47f..0.53f)) {
                    drawRect(
                        color = Color(0xFF00E5FF).copy(alpha = brightness * 0.9f),
                        topLeft = Offset(i * sliceWidth, top),
                        size = Size(sliceWidth + 0.5f, wallHeight)
                    )
                }
            }
        }

        // 3. Render 3D Characters (Multiplayer Network Players or Single-Player Bots)
        renderCharacters3D(game, w, h, depthBuffer, numRays, textMeasurer)

        // 4. Render Particles (Wood chips, fiery explosion embers, sparks)
        for (p in game.particles) {
            val pRelX = p.x - game.playerX
            val pRelY = p.y - game.playerY
            val pDist = sqrt(pRelX * pRelX + pRelY * pRelY)
            if (pDist > 0.3f && pDist < 18f) {
                val pAngle = atan2(pRelY, pRelX)
                var pAngleDiff = pAngle - game.playerAngle
                while (pAngleDiff < -PI) pAngleDiff += (2 * PI).toFloat()
                while (pAngleDiff > PI) pAngleDiff -= (2 * PI).toFloat()

                if (pAngleDiff in -halfFov..halfFov) {
                    val screenX = (w / 2f) + (pAngleDiff / halfFov) * (w / 2f)
                    val screenY = halfH - (p.z / pDist) * 350f
                    val sizeP = (p.size / pDist * 3f).coerceIn(2f, 24f)
                    drawCircle(
                        color = p.color.copy(alpha = p.life.coerceIn(0f, 1f)),
                        radius = sizeP,
                        center = Offset(screenX, screenY)
                    )
                }
            }
        }

        // 5. Floating Text (Hit markers, Damage, Headshot alerts)
        for (ft in game.floatingTexts) {
            val textLayout = textMeasurer.measure(
                text = ft.text,
                style = TextStyle(
                    color = ft.color.copy(alpha = ft.alpha),
                    fontSize = if (ft.isHeadshot) 20.sp else 16.sp,
                    fontWeight = FontWeight.Black
                )
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(ft.x, ft.y)
            )
        }

        // 6. First-Person Viewmodel Weapon & Scope
        renderWeaponViewmodel(game, currentSkin, w, h)

        // 7. Dynamic Tactical Reticle / Crosshair
        renderCrosshair(game, w, h)

        // 8. Tactical Minimap Radar (top-left)
        renderMinimap(game, w, h)
    }
}

private fun castDdaRay(game: TacticalGameEngine, angle: Float): RayHit {
    val cosA = cos(angle)
    val sinA = sin(angle)

    var mapX = floor(game.playerX).toInt()
    var mapY = floor(game.playerY).toInt()

    val deltaDistX = if (cosA == 0f) 1e30f else kotlin.math.abs(1f / cosA)
    val deltaDistY = if (sinA == 0f) 1e30f else kotlin.math.abs(1f / sinA)

    var stepX: Int
    var stepY: Int
    var sideDistX: Float
    var sideDistY: Float

    if (cosA < 0) {
        stepX = -1
        sideDistX = (game.playerX - mapX) * deltaDistX
    } else {
        stepX = 1
        sideDistX = (mapX + 1.0f - game.playerX) * deltaDistX
    }

    if (sinA < 0) {
        stepY = -1
        sideDistY = (game.playerY - mapY) * deltaDistY
    } else {
        stepY = 1
        sideDistY = (mapY + 1.0f - game.playerY) * deltaDistY
    }

    var hit = false
    var side = 0
    var dist = 0f
    var wallType = TileType.EMPTY
    var tileIndex = 0

    var steps = 0
    while (!hit && steps < 30) {
        steps++
        if (sideDistX < sideDistY) {
            sideDistX += deltaDistX
            mapX += stepX
            side = 0
        } else {
            sideDistY += deltaDistY
            mapY += stepY
            side = 1
        }

        if (mapX in 0 until game.mapWidth && mapY in 0 until game.mapHeight) {
            tileIndex = mapY * game.mapWidth + mapX
            val tileId = game.currentGrid[tileIndex]
            val type = TileType.fromId(tileId)
            if (type.isSolid) {
                hit = true
                wallType = type
            }
        } else {
            hit = true
            wallType = TileType.BUNKER_WALL
        }
    }

    dist = if (side == 0) {
        (mapX - game.playerX + (1 - stepX) / 2f) / cosA
    } else {
        (mapY - game.playerY + (1 - stepY) / 2f) / sinA
    }

    val wallX = if (side == 0) {
        game.playerY + dist * sinA - floor(game.playerY + dist * sinA)
    } else {
        game.playerX + dist * cosA - floor(game.playerX + dist * cosA)
    }

    return RayHit(kotlin.math.abs(dist), wallType, side, wallX, tileIndex)
}

private fun DrawScope.renderCharacters3D(
    game: TacticalGameEngine,
    w: Float,
    h: Float,
    depthBuffer: FloatArray,
    numRays: Int,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val halfFov = game.fov / 2f
    val halfH = h / 2f

    if (game.isMultiplayerMode) {
        // Render real network human players
        val otherPlayers = game.networkPlayers
            .filter { it.playerId != game.localPlayerId && it.hp > 0 }
            .sortedByDescending {
                val dx = it.x - game.playerX
                val dy = it.y - game.playerY
                dx * dx + dy * dy
            }

        for (player in otherPlayers) {
            val dx = player.x - game.playerX
            val dy = player.y - game.playerY
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < 0.4f || dist > 25f) continue

            val pAngle = atan2(dy, dx)
            var angleDiff = pAngle - game.playerAngle
            while (angleDiff < -PI) angleDiff += (2 * PI).toFloat()
            while (angleDiff > PI) angleDiff -= (2 * PI).toFloat()

            if (angleDiff in -halfFov..halfFov) {
                val screenX = (w / 2f) + (angleDiff / halfFov) * (w / 2f)
                val rayIdx = ((screenX / w) * numRays).toInt().coerceIn(0, numRays - 1)

                if (dist <= depthBuffer[rayIdx] + 0.6f) {
                    val pHeight = (h / dist * 0.85f).coerceIn(16f, h * 1.5f)
                    val pWidth = pHeight * 0.45f
                    val pTop = halfH - (pHeight * 0.42f)

                    val isTeammate = player.isTeamDelta
                    val bodyColor = if (isTeammate) Color(0xFF00E5FF) else Color(0xFFFF1744)
                    val uniformColor = if (isTeammate) Color(0xFF0F2027) else Color(0xFF2C0B0E)

                    // 1. Uniform Body
                    drawRect(
                        color = uniformColor,
                        topLeft = Offset(screenX - pWidth / 2f, pTop + pHeight * 0.3f),
                        size = Size(pWidth, pHeight * 0.5f)
                    )

                    // 2. Armor Vest
                    drawRect(
                        color = bodyColor.copy(alpha = 0.55f),
                        topLeft = Offset(screenX - pWidth * 0.35f, pTop + pHeight * 0.35f),
                        size = Size(pWidth * 0.7f, pHeight * 0.3f)
                    )

                    // 3. Helmet & Visor
                    drawCircle(
                        color = Color(0xFF0F172A),
                        radius = pWidth * 0.38f,
                        center = Offset(screenX, pTop + pHeight * 0.18f)
                    )
                    drawRect(
                        color = bodyColor,
                        topLeft = Offset(screenX - pWidth * 0.22f, pTop + pHeight * 0.16f),
                        size = Size(pWidth * 0.44f, pHeight * 0.06f)
                    )

                    // 4. Weapon in hands
                    drawRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(screenX + pWidth * 0.1f, pTop + pHeight * 0.45f),
                        size = Size(pWidth * 0.5f, pHeight * 0.1f)
                    )

                    // Firing muzzle flash if shooting
                    if (player.isShooting) {
                        drawCircle(
                            color = Color(0xFFFFD700),
                            radius = pWidth * 0.25f,
                            center = Offset(screenX + pWidth * 0.6f, pTop + pHeight * 0.5f)
                        )
                    }

                    // 5. Nametag & Ping Icon
                    val labelText = "🌐 ${player.playerName}"
                    val labelMeas = textMeasurer.measure(
                        text = labelText,
                        style = TextStyle(
                            color = if (isTeammate) Color(0xFF00E5FF) else Color(0xFFFF5252),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    )
                    drawText(
                        textLayoutResult = labelMeas,
                        topLeft = Offset(screenX - (labelMeas.size.width / 2f), pTop - 22f)
                    )

                    // 6. Health bar
                    val barW = (pWidth * 1.3f).coerceAtLeast(34f)
                    val barH = 5f
                    val hpPct = (player.hp.toFloat() / 100f).coerceIn(0f, 1f)
                    drawRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(screenX - barW / 2f, pTop - 7f),
                        size = Size(barW, barH)
                    )
                    drawRect(
                        color = if (isTeammate) Color(0xFF00E676) else Color(0xFFFF1744),
                        topLeft = Offset(screenX - barW / 2f, pTop - 7f),
                        size = Size(barW * hpPct, barH)
                    )
                }
            }
        }
    } else {
        // Single-player bots rendering
        val sortedBots = game.bots.filter { it.isAlive }.sortedByDescending {
            val dx = it.x - game.playerX
            val dy = it.y - game.playerY
            dx * dx + dy * dy
        }

        for (bot in sortedBots) {
            val dx = bot.x - game.playerX
            val dy = bot.y - game.playerY
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < 0.4f || dist > 22f) continue

            val botAngle = atan2(dy, dx)
            var angleDiff = botAngle - game.playerAngle
            while (angleDiff < -PI) angleDiff += (2 * PI).toFloat()
            while (angleDiff > PI) angleDiff -= (2 * PI).toFloat()

            if (angleDiff in -halfFov..halfFov) {
                val screenX = (w / 2f) + (angleDiff / halfFov) * (w / 2f)
                val rayIdx = ((screenX / w) * numRays).toInt().coerceIn(0, numRays - 1)

                if (dist <= depthBuffer[rayIdx] + 0.5f) {
                    val botHeight = (h / dist * 0.85f).coerceIn(16f, h * 1.5f)
                    val botWidth = botHeight * 0.45f
                    val botTop = halfH - (botHeight * 0.42f)

                    val bodyColor = if (bot.isTeammate) Color(0xFF00E5FF) else Color(0xFFFF1744)
                    val uniformColor = if (bot.isTeammate) Color(0xFF1E293B) else Color(0xFF26181B)

                    // 1. Bot Body / Vest
                    drawRect(
                        color = uniformColor,
                        topLeft = Offset(screenX - botWidth / 2f, botTop + botHeight * 0.3f),
                        size = Size(botWidth, botHeight * 0.5f)
                    )

                    // 2. Tactical Armor Plate / Camo
                    drawRect(
                        color = bodyColor.copy(alpha = 0.4f),
                        topLeft = Offset(screenX - botWidth * 0.35f, botTop + botHeight * 0.35f),
                        size = Size(botWidth * 0.7f, botHeight * 0.3f)
                    )

                    // 3. Helmet & Glowing Visor
                    drawCircle(
                        color = Color(0xFF0F172A),
                        radius = botWidth * 0.38f,
                        center = Offset(screenX, botTop + botHeight * 0.18f)
                    )
                    drawRect(
                        color = bodyColor,
                        topLeft = Offset(screenX - botWidth * 0.22f, botTop + botHeight * 0.16f),
                        size = Size(botWidth * 0.44f, botHeight * 0.06f)
                    )

                    // 4. Weapon in hands
                    drawRect(
                        color = Color(0xFF0A0A0A),
                        topLeft = Offset(screenX + botWidth * 0.1f, botTop + botHeight * 0.45f),
                        size = Size(botWidth * 0.5f, botHeight * 0.1f)
                    )

                    // 5. Bot Nametag
                    val labelText = "${bot.platform.iconText} ${bot.clanTag} ${bot.name}"
                    val labelMeas = textMeasurer.measure(
                        text = labelText,
                        style = TextStyle(
                            color = if (bot.isTeammate) Color(0xFF00E5FF) else Color(0xFFFF5252),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawText(
                        textLayoutResult = labelMeas,
                        topLeft = Offset(screenX - (labelMeas.size.width / 2f), botTop - 20f)
                    )

                    // 6. Health bar above bot
                    val barW = (botWidth * 1.2f).coerceAtLeast(30f)
                    val barH = 4f
                    val hpPct = (bot.hp.toFloat() / bot.maxHp.toFloat()).coerceIn(0f, 1f)
                    drawRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(screenX - barW / 2f, botTop - 6f),
                        size = Size(barW, barH)
                    )
                    drawRect(
                        color = if (bot.isTeammate) Color(0xFF00E676) else Color(0xFFFF1744),
                        topLeft = Offset(screenX - barW / 2f, botTop - 6f),
                        size = Size(barW * hpPct, barH)
                    )
                }
            }
        }
    }
}

private fun DrawScope.renderWeaponViewmodel(
    game: TacticalGameEngine,
    skin: WeaponSkin?,
    w: Float,
    h: Float
) {
    if (game.isAds) {
        // Sniper/Holographic ADS Zoom Overlay
        drawRect(
            color = Color(0x99000000),
            size = Size(w, h)
        )
        val center = Offset(w / 2f, h / 2f)
        val scopeR = (w * 0.38f).coerceAtMost(h * 0.42f)

        // Clear scope viewport circle with high-contrast reticle
        drawCircle(
            color = Color(0xFF00E5FF),
            radius = scopeR,
            center = center,
            style = Stroke(width = 3f)
        )
        // Mil-dot crosshairs in scope
        drawLine(
            color = Color(0xFFFF1744),
            start = Offset(center.x - scopeR, center.y),
            end = Offset(center.x + scopeR, center.y),
            strokeWidth = 1.5f
        )
        drawLine(
            color = Color(0xFFFF1744),
            start = Offset(center.x, center.y - scopeR),
            end = Offset(center.x, center.y + scopeR),
            strokeWidth = 1.5f
        )
        drawCircle(
            color = Color(0xFFFF1744),
            radius = 3.5f,
            center = center
        )
        return
    }

    // Normal First-Person Viewmodel Gun Model (Bottom Right)
    val recoilY = game.weaponRecoilOffset * 28f
    val recoilX = game.weaponRecoilOffset * 8f

    val gunBaseX = w * 0.62f + recoilX
    val gunBaseY = h * 0.60f + recoilY

    val primarySkinColor = skin?.let { Color(it.primaryColor) } ?: Color(0xFF263238)
    val accentSkinColor = skin?.let { Color(it.accentColor) } ?: Color(0xFF00E5FF)

    val gunPath = Path().apply {
        moveTo(gunBaseX, h)
        lineTo(gunBaseX + 30f, gunBaseY + 60f)
        lineTo(gunBaseX - 45f, gunBaseY + 30f)
        lineTo(gunBaseX - 70f, gunBaseY - 20f)
        lineTo(gunBaseX - 10f, gunBaseY - 25f)
        lineTo(gunBaseX + 60f, gunBaseY + 40f)
        lineTo(w + 40f, h)
        close()
    }
    drawPath(path = gunPath, color = primarySkinColor)

    // Skin Accent Stripe / Pattern
    val stripePath = Path().apply {
        moveTo(gunBaseX - 60f, gunBaseY - 10f)
        lineTo(gunBaseX - 20f, gunBaseY - 15f)
        lineTo(gunBaseX + 40f, gunBaseY + 45f)
        lineTo(gunBaseX + 20f, gunBaseY + 55f)
        close()
    }
    drawPath(path = stripePath, color = accentSkinColor)

    // Holographic Reflex Sight on Top of Gun
    val sightX = gunBaseX - 35f
    val sightY = gunBaseY - 35f
    drawRect(
        color = Color(0xFF111827),
        topLeft = Offset(sightX, sightY),
        size = Size(36f, 18f)
    )
    drawRect(
        color = Color(0xFF00E5FF).copy(alpha = 0.4f),
        topLeft = Offset(sightX + 6f, sightY - 18f),
        size = Size(24f, 18f)
    )
    // Red dot inside reflex sight
    drawCircle(
        color = Color(0xFFFF1744),
        radius = 2.5f,
        center = Offset(sightX + 18f, sightY - 9f)
    )

    // Muzzle Flash Star Burst
    if (game.muzzleFlashTimer > 0) {
        val muzzleX = gunBaseX - 75f
        val muzzleY = gunBaseY - 22f
        drawCircle(
            color = Color(0xFFFFF9C4),
            radius = 28f,
            center = Offset(muzzleX, muzzleY)
        )
        drawCircle(
            color = Color(0xFFFF9100),
            radius = 18f,
            center = Offset(muzzleX, muzzleY)
        )
        // Flash light ray flares
        drawLine(
            color = Color(0xFFFFFF00),
            start = Offset(muzzleX - 35f, muzzleY),
            end = Offset(muzzleX + 35f, muzzleY),
            strokeWidth = 3f
        )
        drawLine(
            color = Color(0xFFFFFF00),
            start = Offset(muzzleX, muzzleY - 35f),
            end = Offset(muzzleX, muzzleY + 35f),
            strokeWidth = 3f
        )
    }
}

private fun DrawScope.renderCrosshair(
    game: TacticalGameEngine,
    w: Float,
    h: Float
) {
    if (game.isAds) return // Scope reticle already drawn

    val cx = w / 2f
    val cy = h / 2f
    val spread = 8f + (game.weaponRecoilOffset * 22f)
    val len = 9f
    val chColor = Color(0xFF00E5FF)

    // Dynamic 4-line Crosshair
    // Top
    drawLine(chColor, Offset(cx, cy - spread - len), Offset(cx, cy - spread), strokeWidth = 2f)
    // Bottom
    drawLine(chColor, Offset(cx, cy + spread), Offset(cx, cy + spread + len), strokeWidth = 2f)
    // Left
    drawLine(chColor, Offset(cx - spread - len, cy), Offset(cx - spread, cy), strokeWidth = 2f)
    // Right
    drawLine(chColor, Offset(cx + spread, cy), Offset(cx + spread + len, cy), strokeWidth = 2f)

    // Center dot
    drawCircle(chColor, radius = 1.5f, center = Offset(cx, cy))

    // Hit Marker (White on normal hit, Crimson on critical headshot)
    if (game.hitMarkerTimer > 0) {
        val hitColor = if (game.hitMarkerIsCrit) Color(0xFFFF1744) else Color.White
        val hmSize = if (game.hitMarkerIsCrit) 14f else 9f
        val strokeW = if (game.hitMarkerIsCrit) 3f else 2f

        drawLine(hitColor, Offset(cx - hmSize, cy - hmSize), Offset(cx - 3f, cy - 3f), strokeWidth = strokeW)
        drawLine(hitColor, Offset(cx + 3f, cy - 3f), Offset(cx + hmSize, cy - hmSize), strokeWidth = strokeW)
        drawLine(hitColor, Offset(cx - hmSize, cy + hmSize), Offset(cx - 3f, cy + 3f), strokeWidth = strokeW)
        drawLine(hitColor, Offset(cx + 3f, cy + 3f), Offset(cx + hmSize, cy + hmSize), strokeWidth = strokeW)
    }
}

private fun DrawScope.renderMinimap(
    game: TacticalGameEngine,
    w: Float,
    h: Float
) {
    val radarRadius = 45f
    val radarCenter = Offset(60f, 65f)

    // Radar Dark Base
    drawCircle(
        color = Color(0xCC0B111E),
        radius = radarRadius,
        center = radarCenter
    )
    drawCircle(
        color = Color(0xFF00E5FF).copy(alpha = 0.4f),
        radius = radarRadius,
        center = radarCenter,
        style = Stroke(width = 1.5f)
    )

    // Sweep rings
    drawCircle(
        color = Color(0x3300E5FF),
        radius = radarRadius * 0.55f,
        center = radarCenter,
        style = Stroke(width = 1f)
    )

    val scale = radarRadius / 10f

    // Draw Destructibles (Crates, Barrels) as orange blips on radar
    for ((_, dest) in game.destructibles) {
        if (!dest.isDestroyed) {
            val rdx = (dest.x + 0.5f - game.playerX) * scale
            val rdy = (dest.y + 0.5f - game.playerY) * scale
            val rDist = sqrt(rdx * rdx + rdy * rdy)
            if (rDist < radarRadius - 4f) {
                drawRect(
                    color = if (dest.type == TileType.EXPLOSIVE_BARREL) Color(0xFFFF3D00) else Color(0xFFFFB300),
                    topLeft = Offset(radarCenter.x + rdx - 2f, radarCenter.y + rdy - 2f),
                    size = Size(4f, 4f)
                )
            }
        }
    }

    // Draw Bots (Teammates cyan, Enemies red)
    for (bot in game.bots) {
        if (!bot.isAlive) continue
        val bdx = (bot.x - game.playerX) * scale
        val bdy = (bot.y - game.playerY) * scale
        val bDist = sqrt(bdx * bdx + bdy * bdy)
        if (bDist < radarRadius - 4f) {
            drawCircle(
                color = if (bot.isTeammate) Color(0xFF00E5FF) else Color(0xFFFF1744),
                radius = 3.2f,
                center = Offset(radarCenter.x + bdx, radarCenter.y + bdy)
            )
        }
    }

    // Player arrow (Cyan pointer facing direction)
    val tipX = radarCenter.x + cos(game.playerAngle) * 7f
    val tipY = radarCenter.y + sin(game.playerAngle) * 7f
    drawLine(
        color = Color(0xFF00E5FF),
        start = radarCenter,
        end = Offset(tipX, tipY),
        strokeWidth = 2.5f
    )
    drawCircle(
        color = Color.White,
        radius = 2f,
        center = radarCenter
    )
}
