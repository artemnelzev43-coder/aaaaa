package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Vanguard_SpecOps",
    val email: String = "simalpoal@gmail.com",
    val isGoogleLinked: Boolean = false,
    val googlePlayerId: String? = null,
    val level: Int = 5,
    val xp: Int = 1250,
    val coins: Int = 3200,
    val wins: Int = 14,
    val losses: Int = 6,
    val kills: Int = 89,
    val deaths: Int = 42,
    val headshots: Int = 34,
    val cratesDestroyed: Int = 28,
    val casesOpened: Int = 4,
    val clanId: String = "clan_apex",
    val clanTag: String = "[APEX]",
    val referralCode: String = "",
    val invitedCount: Int = 0,
    val lastDailyClaimDate: String = "",
    val dailyConsecutiveDays: Int = 0,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val isPushEnabled: Boolean = true,
    val voiceEnabled: Boolean = true
)

@Entity(tableName = "owned_skins")
data class OwnedSkinEntity(
    @PrimaryKey val skinId: String,
    val weaponId: String,
    val isEquipped: Boolean
)

@Entity(tableName = "challenges_progress")
data class ChallengeProgressEntity(
    @PrimaryKey val challengeId: String,
    val currentProgress: Int,
    val isClaimed: Boolean
)

@Entity(tableName = "used_gift_codes")
data class UsedGiftCodeEntity(
    @PrimaryKey val code: String,
    val redeemedAt: Long = System.currentTimeMillis()
)

