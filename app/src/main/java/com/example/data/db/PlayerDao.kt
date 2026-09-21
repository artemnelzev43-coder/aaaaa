package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun getPlayerProfile(): Flow<PlayerProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayerProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM owned_skins")
    fun getOwnedSkins(): Flow<List<OwnedSkinEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOwnedSkin(skin: OwnedSkinEntity)

    @Query("UPDATE owned_skins SET isEquipped = 0 WHERE weaponId = :weaponId")
    suspend fun unequipAllSkinsForWeapon(weaponId: String)

    @Query("UPDATE owned_skins SET isEquipped = 1 WHERE skinId = :skinId")
    suspend fun setSkinEquipped(skinId: String)

    @Transaction
    suspend fun equipSkinForWeapon(weaponId: String, skinId: String) {
        unequipAllSkinsForWeapon(weaponId)
        setSkinEquipped(skinId)
    }

    @Query("SELECT * FROM challenges_progress")
    fun getChallengeProgressList(): Flow<List<ChallengeProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChallengeProgress(progress: ChallengeProgressEntity)

    @Query("SELECT COUNT(*) > 0 FROM used_gift_codes WHERE code = :code")
    suspend fun isGiftCodeUsed(code: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markGiftCodeUsed(entity: UsedGiftCodeEntity)
}
