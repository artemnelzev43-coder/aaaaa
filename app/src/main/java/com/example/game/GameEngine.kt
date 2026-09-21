package com.example.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.data.model.PlatformType
import com.example.network.PlayerNetworkState
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class TileType(val id: Int, val isSolid: Boolean, val isDestructible: Boolean, val maxHp: Int) {
    EMPTY(0, false, false, 0),
    BUNKER_WALL(1, true, false, 9999),
    CYBER_STEEL(2, true, false, 9999),
    WOODEN_CRATE(3, true, true, 60),
    EXPLOSIVE_BARREL(4, true, true, 40),
    SANDBAG_COVER(5, true, true, 100),
    BOMB_SITE_A(6, false, false, 0),
    BOMB_SITE_B(7, false, false, 0);

    companion object {
        fun fromId(id: Int): TileType = entries.find { it.id == id } ?: EMPTY
    }
}

data class DestructibleTile(
    val x: Int,
    val y: Int,
    val type: TileType,
    var currentHp: Int,
    var isDestroyed: Boolean = false
)

data class Particle(
    var x: Float,
    var y: Float,
    var z: Float,
    var vx: Float,
    var vy: Float,
    var vz: Float,
    val color: Color,
    val size: Float,
    var life: Float = 1.0f,
    val decay: Float = 0.05f
)

data class FloatingText(
    val text: String,
    var x: Float,
    var y: Float,
    val color: Color,
    var alpha: Float = 1.0f,
    val isHeadshot: Boolean = false
)

data class EnemyBot(
    val id: String,
    val name: String,
    val clanTag: String,
    val platform: PlatformType,
    var x: Float,
    var y: Float,
    var angle: Float,
    var hp: Int = 100,
    val maxHp: Int = 100,
    var isAlive: Boolean = true,
    var shootCooldownMs: Long = 0L,
    var moveTargetX: Float = 0f,
    var moveTargetY: Float = 0f,
    val isTeammate: Boolean = false
)

class TacticalGameEngine {

    // 16x16 Tactical Arena Map
    val mapWidth = 16
    val mapHeight = 16

    val initialMap = intArrayOf(
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 0, 0, 0, 3, 0, 0, 1, 0, 0, 3, 0, 0, 6, 0, 1,
        1, 0, 2, 0, 3, 0, 0, 2, 0, 0, 0, 0, 2, 0, 0, 1,
        1, 0, 2, 0, 0, 0, 4, 0, 0, 4, 3, 0, 2, 0, 0, 1,
        1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1,
        1, 3, 3, 0, 1, 0, 0, 0, 0, 1, 0, 5, 5, 0, 0, 1,
        1, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
        1, 0, 1, 0, 0, 3, 0, 0, 0, 3, 0, 0, 1, 0, 0, 1,
        1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1,
        1, 0, 0, 0, 5, 5, 0, 4, 0, 0, 0, 0, 0, 0, 0, 1,
        1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 3, 3, 0, 0, 1,
        1, 0, 2, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1,
        1, 0, 2, 0, 0, 4, 0, 0, 0, 4, 0, 0, 2, 0, 0, 1,
        1, 0, 0, 0, 0, 0, 0, 2, 0, 0, 3, 0, 2, 0, 0, 1,
        1, 7, 0, 0, 3, 0, 0, 1, 0, 0, 3, 0, 0, 0, 0, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
    )

    // Dynamic grid tiles and destructible registry
    val currentGrid = initialMap.clone()
    val destructibles = mutableMapOf<Int, DestructibleTile>()

    // Mode: Multiplayer vs Single-player
    var isMultiplayerMode by mutableStateOf(false)
    var localPlayerId by mutableStateOf("local_p1")
    var networkPlayers by mutableStateOf<List<PlayerNetworkState>>(emptyList())
    var onNetworkHitCallback: ((targetId: String, damage: Int, isCrit: Boolean) -> Unit)? = null

    // Player State
    var playerX by mutableFloatStateOf(2.5f)
    var playerY by mutableFloatStateOf(2.5f)
    var playerAngle by mutableFloatStateOf(0.8f)
    var playerHp by mutableIntStateOf(100)
    var playerArmor by mutableIntStateOf(100)
    var isPlayerAlive by mutableStateOf(true)
    var fov by mutableFloatStateOf((PI / 3).toFloat()) // ~60 degrees
    var isAds by mutableStateOf(false) // Aim Down Sights (Zoom)

    // Weapon state
    var currentWeaponId by mutableStateOf("w_ak12")
    var currentMagAmmo by mutableIntStateOf(30)
    var currentReserveAmmo by mutableIntStateOf(120)
    var isReloading by mutableStateOf(false)
    var reloadProgress by mutableFloatStateOf(0f)
    var weaponRecoilOffset by mutableFloatStateOf(0f)
    var muzzleFlashTimer by mutableIntStateOf(0)
    var hitMarkerTimer by mutableIntStateOf(0)
    var hitMarkerIsCrit by mutableStateOf(false)

    // Economy & Round Stats
    var roundCredits by mutableIntStateOf(2800)
    var matchKills by mutableIntStateOf(0)
    var matchDeaths by mutableIntStateOf(0)
    var matchHeadshots by mutableIntStateOf(0)
    var matchCratesDestroyed by mutableIntStateOf(0)
    var teamScoreDelta by mutableIntStateOf(0)
    var teamScoreSpectre by mutableIntStateOf(0)
    var roundTimeRemainingSec by mutableIntStateOf(120)

    // Render / Game Loop Tick for Canvas recomposition
    var renderTick by mutableIntStateOf(0)

    // Continuous Input State (Joystick / Buttons)
    var inputForward by mutableFloatStateOf(0f)
    var inputStrafe by mutableFloatStateOf(0f)

    // Visual FX
    val particles = mutableListOf<Particle>()
    val floatingTexts = mutableListOf<FloatingText>()

    // Bots & Squadmates (Single player mode only)
    val bots = mutableListOf<EnemyBot>()

    // Killfeed
    val killFeed = mutableListOf<String>()

    init {
        resetMatch(isMultiplayer = false)
    }

    fun resetMatch(isMultiplayer: Boolean = false) {
        isMultiplayerMode = isMultiplayer
        // Reset grid
        for (i in initialMap.indices) {
            currentGrid[i] = initialMap[i]
            val type = TileType.fromId(initialMap[i])
            if (type.isDestructible) {
                destructibles[i] = DestructibleTile(
                    x = i % mapWidth,
                    y = i / mapWidth,
                    type = type,
                    currentHp = type.maxHp
                )
            }
        }

        // Reset player
        playerX = if (isMultiplayer) 2.5f else 2.5f
        playerY = if (isMultiplayer) 2.5f else 2.5f
        playerAngle = 0.78f
        playerHp = 100
        playerArmor = 100
        isPlayerAlive = true
        isAds = false
        currentMagAmmo = 30
        currentReserveAmmo = 120
        weaponRecoilOffset = 0f
        muzzleFlashTimer = 0
        hitMarkerTimer = 0
        particles.clear()
        floatingTexts.clear()
        killFeed.clear()
        teamScoreDelta = 0
        teamScoreSpectre = 0

        // Bots logic: in multiplayer, NO BOTS!
        bots.clear()
        if (!isMultiplayer) {
            // Single-player practice bots
            bots.add(
                EnemyBot("bot_team_1", "Delta_Viper", "[APEX]", PlatformType.PC, 3.5f, 2.5f, 0.5f, isTeammate = true)
            )
            bots.add(
                EnemyBot("bot_enemy_1", "Shadow_Sniper", "[SPECTRE]", PlatformType.PC, 13.5f, 2.5f, 3.14f, isTeammate = false)
            )
            bots.add(
                EnemyBot("bot_enemy_2", "Vortex_Assault", "[SPECTRE]", PlatformType.IOS, 8.5f, 6.5f, 2.5f, isTeammate = false)
            )
        }
    }

    fun updateGame(dtSec: Float) {
        if (!isPlayerAlive) return

        // Process Continuous Player Movement
        if (inputForward != 0f || inputStrafe != 0f) {
            movePlayer(inputForward, inputStrafe, dtSec)
        }

        // Recoil recovery
        if (weaponRecoilOffset > 0f) {
            weaponRecoilOffset = max(0f, weaponRecoilOffset - dtSec * 4f)
        }

        if (muzzleFlashTimer > 0) muzzleFlashTimer--
        if (hitMarkerTimer > 0) hitMarkerTimer--

        // Reload logic
        if (isReloading) {
            reloadProgress += dtSec * 0.8f
            if (reloadProgress >= 1.0f) {
                isReloading = false
                reloadProgress = 0f
                val needed = 30 - currentMagAmmo
                val toAdd = min(needed, currentReserveAmmo)
                currentMagAmmo += toAdd
                currentReserveAmmo -= toAdd
            }
        }

        // Particles update
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dtSec
            p.y += p.vy * dtSec
            p.z += p.vz * dtSec
            p.vz -= 9.8f * dtSec // gravity
            p.life -= p.decay
            if (p.life <= 0f) pIter.remove()
        }

        // Floating texts update
        val tIter = floatingTexts.iterator()
        while (tIter.hasNext()) {
            val t = tIter.next()
            t.y -= 25f * dtSec
            t.alpha -= dtSec * 0.7f
            if (t.alpha <= 0f) tIter.remove()
        }

        // Single-player bots AI (only if NOT in multiplayer)
        if (!isMultiplayerMode) {
            for (bot in bots) {
                if (!bot.isAlive) continue

                val dx = playerX - bot.x
                val dy = playerY - bot.y
                val dist = sqrt(dx * dx + dy * dy)
                bot.angle = atan2(dy, dx)

                if (!bot.isTeammate && dist < 9f && hasLineOfSight(bot.x, bot.y, playerX, playerY)) {
                    bot.shootCooldownMs += (dtSec * 1000).toLong()
                    if (bot.shootCooldownMs > 1100L) {
                        bot.shootCooldownMs = 0L
                        val damage = Random.nextInt(8, 16)
                        takePlayerDamage(damage, bot.name)
                    }
                }
            }
        }

        renderTick++
    }

    private fun hasLineOfSight(x0: Float, y0: Float, x1: Float, y1: Float): Boolean {
        val dx = x1 - x0
        val dy = y1 - y0
        val dist = sqrt(dx * dx + dy * dy)
        val steps = (dist * 4).toInt()
        for (i in 1 until steps) {
            val t = i.toFloat() / steps
            val checkX = x0 + dx * t
            val checkY = y0 + dy * t
            if (isWall(checkX, checkY)) return false
        }
        return true
    }

    fun takePlayerDamage(damage: Int, attackerName: String) {
        if (!isPlayerAlive) return

        var actualDamage = damage
        if (playerArmor > 0) {
            val absorbed = min(playerArmor, (damage * 0.6f).toInt())
            playerArmor -= absorbed
            actualDamage -= absorbed
        }

        playerHp -= actualDamage
        spawnHitParticles(playerX, playerY, Color.Red, count = 5)

        if (playerHp <= 0) {
            playerHp = 0
            isPlayerAlive = false
            matchDeaths++
            teamScoreSpectre++
            killFeed.add("💀 $attackerName ликвидировал Вас")
        }
    }

    // Walking / Movement with reliable sliding collision detection
    fun movePlayer(forward: Float, strafe: Float, dtSec: Float) {
        if (!isPlayerAlive) return
        val speed = if (isAds) 2.0f else 3.8f

        val moveAngle = playerAngle
        val forwardX = cos(moveAngle) * forward * speed * dtSec
        val forwardY = sin(moveAngle) * forward * speed * dtSec

        val strafeAngle = moveAngle + (PI / 2).toFloat()
        val strafeX = cos(strafeAngle) * strafe * speed * dtSec
        val strafeY = sin(strafeAngle) * strafe * speed * dtSec

        val deltaX = forwardX + strafeX
        val deltaY = forwardY + strafeY

        val targetX = playerX + deltaX
        val targetY = playerY + deltaY

        val r = 0.22f

        // Smooth X-axis collision check and sliding
        if (deltaX != 0f) {
            val checkX = if (deltaX > 0) targetX + r else targetX - r
            if (!isWall(checkX, playerY - r * 0.7f) && !isWall(checkX, playerY + r * 0.7f)) {
                playerX = targetX.coerceIn(1.1f, mapWidth - 1.1f)
            }
        }

        // Smooth Y-axis collision check and sliding
        if (deltaY != 0f) {
            val checkY = if (deltaY > 0) targetY + r else targetY - r
            if (!isWall(playerX - r * 0.7f, checkY) && !isWall(playerX + r * 0.7f, checkY)) {
                playerY = targetY.coerceIn(1.1f, mapHeight - 1.1f)
            }
        }

        renderTick++
    }

    fun stepForward() {
        movePlayer(1f, 0f, 0.12f)
    }

    fun stepBackward() {
        movePlayer(-1f, 0f, 0.12f)
    }

    fun stepStrafeLeft() {
        movePlayer(0f, -1f, 0.12f)
    }

    fun stepStrafeRight() {
        movePlayer(0f, 1f, 0.12f)
    }

    fun rotatePlayer(deltaAngle: Float) {
        playerAngle += deltaAngle
        while (playerAngle < 0) playerAngle += (2 * PI).toFloat()
        while (playerAngle >= 2 * PI) playerAngle -= (2 * PI).toFloat()
        renderTick++
    }

    fun toggleAds() {
        isAds = !isAds
        fov = if (isAds) (PI / 5).toFloat() else (PI / 3).toFloat()
    }

    fun startReload() {
        if (!isReloading && currentMagAmmo < 30 && currentReserveAmmo > 0) {
            isReloading = true
            reloadProgress = 0f
        }
    }

    fun fireWeapon(weaponDamage: Int, weaponRecoil: Float): Boolean {
        if (!isPlayerAlive || isReloading || currentMagAmmo <= 0) return false

        currentMagAmmo--
        muzzleFlashTimer = 3
        weaponRecoilOffset = min(1.0f, weaponRecoilOffset + weaponRecoil)

        val spreadOffset = (Random.nextFloat() - 0.5f) * (if (isAds) 0.012f else 0.035f) * (1f + weaponRecoilOffset)
        val bulletAngle = playerAngle + spreadOffset

        castBulletHitscan(bulletAngle, weaponDamage)
        return true
    }

    fun throwGrenade() {
        val gx = playerX + cos(playerAngle) * 4.5f
        val gy = playerY + sin(playerAngle) * 4.5f
        detonateExplosion(gx, gy, radius = 3.5f, maxDamage = 130)
    }

    private fun castBulletHitscan(angle: Float, baseDamage: Int) {
        // In multiplayer mode, test hits against real network players
        if (isMultiplayerMode) {
            for (p in networkPlayers) {
                if (p.playerId == localPlayerId || p.hp <= 0) continue

                val bdx = p.x - playerX
                val bdy = p.y - playerY
                val pAngle = atan2(bdy, bdx)
                val angleDiff = bulletAngleDiff(angle, pAngle)

                val dist = sqrt(bdx * bdx + bdy * bdy)
                val apparentWidth = 0.55f / max(0.5f, dist)

                if (kotlin.math.abs(angleDiff) < apparentWidth && dist < 22f && hasLineOfSight(playerX, playerY, p.x, p.y)) {
                    val isCrit = Random.nextFloat() < 0.35f
                    val damage = if (isCrit) (baseDamage * 2.1f).toInt() else baseDamage
                    hitMarkerTimer = 6
                    hitMarkerIsCrit = isCrit

                    spawnHitParticles(p.x, p.y, Color(0xFFFF1744), count = 6)
                    onNetworkHitCallback?.invoke(p.playerId, damage, isCrit)

                    floatingTexts.add(
                        FloatingText(
                            text = if (isCrit) "В ГОЛОВУ! -$damage" else "-$damage",
                            x = 350f,
                            y = 280f,
                            color = if (isCrit) Color(0xFFFF1744) else Color(0xFFFFD700),
                            isHeadshot = isCrit
                        )
                    )
                    return
                }
            }
        } else {
            // Single player practice bots hitscan
            for (bot in bots) {
                if (!bot.isAlive || bot.isTeammate) continue

                val bdx = bot.x - playerX
                val bdy = bot.y - playerY
                val botAngle = atan2(bdy, bdx)
                val angleDiff = bulletAngleDiff(angle, botAngle)

                val dist = sqrt(bdx * bdx + bdy * bdy)
                val apparentWidth = 0.45f / max(0.5f, dist)

                if (kotlin.math.abs(angleDiff) < apparentWidth && dist < 20f && hasLineOfSight(playerX, playerY, bot.x, bot.y)) {
                    val isCrit = Random.nextFloat() < 0.35f
                    val damage = if (isCrit) (baseDamage * 2.2f).toInt() else baseDamage
                    hitMarkerTimer = 6
                    hitMarkerIsCrit = isCrit

                    damageBot(bot, damage, isCrit, fromPlayer = true)
                    return
                }
            }
        }

        // Raycast against destructible obstacles & walls
        val cosA = cos(angle)
        val sinA = sin(angle)
        var t = 0.5f
        while (t < 20f) {
            t += 0.2f
            val cx = playerX + cosA * t
            val cy = playerY + sinA * t
            val gx = cx.toInt()
            val gy = cy.toInt()

            if (gx in 0 until mapWidth && gy in 0 until mapHeight) {
                val index = gy * mapWidth + gx
                val tileType = TileType.fromId(currentGrid[index])

                if (tileType.isSolid) {
                    if (tileType.isDestructible) {
                        damageDestructible(index, baseDamage)
                    } else {
                        spawnHitParticles(cx, cy, Color(0xFF00E5FF), count = 4)
                    }
                    break
                }
            }
        }
    }

    private fun damageBot(bot: EnemyBot, damage: Int, isCrit: Boolean, fromPlayer: Boolean) {
        bot.hp -= damage
        if (bot.hp <= 0) {
            bot.hp = 0
            bot.isAlive = false

            if (fromPlayer) {
                matchKills++
                if (isCrit) matchHeadshots++
                val reward = if (isCrit) 450 else 300
                roundCredits += reward
                killFeed.add("⚡ Вы ликвидировали ${bot.name} (+$reward$)")
                teamScoreDelta++
                floatingTexts.add(
                    FloatingText(
                        text = "ВРАГ УНИЧТОЖЕН! +$reward$",
                        x = 350f,
                        y = 300f,
                        color = Color(0xFF00E676)
                    )
                )
            } else {
                killFeed.add("🎯 Delta Squad ликвидировал ${bot.name}")
            }
        }
    }

    fun damageDestructible(tileIndex: Int, damage: Int) {
        val destructible = destructibles[tileIndex] ?: return
        destructible.currentHp -= damage
        hitMarkerTimer = 6
        hitMarkerIsCrit = false

        val worldX = destructible.x + 0.5f
        val worldY = destructible.y + 0.5f

        if (destructible.currentHp <= 0 && !destructible.isDestroyed) {
            destructible.isDestroyed = true
            currentGrid[tileIndex] = TileType.EMPTY.id
            matchCratesDestroyed++
            roundCredits += 50

            if (destructible.type == TileType.EXPLOSIVE_BARREL) {
                detonateExplosion(worldX, worldY, radius = 4f, maxDamage = 150)
                killFeed.add("💥 ВЗРЫВ БОЧКИ: Укрытие уничтожено!")
            } else {
                spawnHitParticles(worldX, worldY, Color(0xFF8D6E63), count = 12)
                killFeed.add("📦 Ящик разрушен")
            }
        } else {
            spawnHitParticles(worldX, worldY, Color(0xFFFF9800), count = 5)
        }
    }

    private fun detonateExplosion(centerX: Float, centerY: Float, radius: Float, maxDamage: Int) {
        spawnHitParticles(centerX, centerY, Color(0xFFFF3D00), count = 25)
        spawnHitParticles(centerX, centerY, Color(0xFFFFEA00), count = 15)

        if (!isMultiplayerMode) {
            for (bot in bots) {
                if (!bot.isAlive) continue
                val dist = sqrt((bot.x - centerX) * (bot.x - centerX) + (bot.y - centerY) * (bot.y - centerY))
                if (dist < radius) {
                    val dmg = ((1f - dist / radius) * maxDamage).toInt()
                    damageBot(bot, dmg, isCrit = false, fromPlayer = true)
                }
            }
        }

        // Damage player if in range
        val pDist = sqrt((playerX - centerX) * (playerX - centerX) + (playerY - centerY) * (playerY - centerY))
        if (pDist < radius) {
            val pDmg = ((1f - pDist / radius) * maxDamage * 0.7f).toInt()
            takePlayerDamage(pDmg, "Взрыв")
        }
    }

    private fun spawnHitParticles(x: Float, y: Float, color: Color, count: Int) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2 * PI.toFloat()
            val speed = Random.nextFloat() * 4f + 1f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    z = 0.5f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    vz = Random.nextFloat() * 3f + 1f,
                    color = color,
                    size = Random.nextFloat() * 6f + 4f,
                    life = 1.0f,
                    decay = Random.nextFloat() * 0.04f + 0.03f
                )
            )
        }
    }

    private fun bulletAngleDiff(a: Float, b: Float): Float {
        var diff = a - b
        while (diff < -PI) diff += (2 * PI).toFloat()
        while (diff > PI) diff -= (2 * PI).toFloat()
        return diff
    }

    fun isWall(x: Float, y: Float): Boolean {
        val gx = floor(x).toInt()
        val gy = floor(y).toInt()
        if (gx < 0 || gx >= mapWidth || gy < 0 || gy >= mapHeight) return true
        val tileId = currentGrid[gy * mapWidth + gx]
        return TileType.fromId(tileId).isSolid
    }
}
