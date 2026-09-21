package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class WeaponCategory(val displayName: String) {
    PISTOL("Пистолеты"),
    SMG("ПП"),
    RIFLE("Винтовки"),
    SNIPER("Снайперские"),
    SHOTGUN("Дробовики"),
    GEAR("Снаряжение")
}

enum class SkinRarity(val title: String, val color: Color, val multiplier: Float) {
    COMMON("Обычный", Color(0xFF78909C), 1.0f),
    RARE("Редкий", Color(0xFF29B6F6), 1.5f),
    EPIC("Эпический", Color(0xFFAB47BC), 2.5f),
    LEGENDARY("Легендарный", Color(0xFFFF1744), 5.0f),
    MYTHIC("Мифический", Color(0xFFFFD700), 10.0f)
}

data class WeaponSkin(
    val id: String,
    val weaponId: String,
    val name: String,
    val rarity: SkinRarity,
    val primaryColor: Long,
    val accentColor: Long,
    val isOwned: Boolean = false,
    val isEquipped: Boolean = false,
    val description: String = ""
)

data class Weapon(
    val id: String,
    val name: String,
    val category: WeaponCategory,
    val price: Int,
    val damage: Int,
    val fireRateMs: Long,
    val magSize: Int,
    val maxReserveAmmo: Int,
    val recoil: Float,
    val spread: Float,
    val range: Float,
    val isDefaultUnlocked: Boolean = false,
    val skins: List<WeaponSkin> = emptyList(),
    val description: String = ""
)

data class WeaponCase(
    val id: String,
    val name: String,
    val price: Int,
    val colorHex: Long,
    val description: String,
    val availableSkinIds: List<String>
)

enum class PlatformType(val displayName: String, val iconText: String) {
    ANDROID("Android", "📱"),
    PC("PC / Steam", "💻"),
    IOS("iOS", "🍏"),
    PLAYSTATION("PS5", "🎮"),
    XBOX("Xbox", "🟩")
}

data class LeaderboardPlayer(
    val rank: Int,
    val name: String,
    val clanTag: String,
    val elo: Int,
    val kills: Int,
    val kdRatio: Float,
    val platform: PlatformType,
    val isCurrentPlayer: Boolean = false
)

data class Clan(
    val id: String,
    val name: String,
    val tag: String,
    val level: Int,
    val membersCount: Int,
    val maxMembers: Int = 30,
    val clanRating: Int,
    val description: String,
    val isUserMember: Boolean = false
)

data class ClanMember(
    val name: String,
    val role: String,
    val score: Int,
    val platform: PlatformType,
    val isOnline: Boolean
)

data class WeeklyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val current: Int,
    val goal: Int,
    val coinReward: Int,
    val xpReward: Int,
    val isClaimed: Boolean = false
) {
    val isCompleted: Boolean get() = current >= goal
    val progressFraction: Float get() = (current.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
}

data class DailyReward(
    val day: Int,
    val coins: Int,
    val xp: Int,
    val bonusDescription: String,
    val isClaimed: Boolean,
    val isAvailableToday: Boolean
)

data class PlayAchievement(
    val id: String,
    val title: String,
    val description: String,
    val xpValue: Int,
    val isUnlocked: Boolean,
    val progress: Int,
    val maxProgress: Int,
    val iconEmoji: String
)

data class AntiCheatReport(
    val id: String,
    val reportedPlayer: String,
    val reason: String,
    val timestamp: String,
    val status: String
)
