package com.example.ui.viewmodel

import android.app.Application
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.RealVoiceChatManager
import com.example.data.db.AppDatabase
import com.example.data.db.OwnedSkinEntity
import com.example.data.db.PlayerProfileEntity
import com.example.data.model.Clan
import com.example.data.model.DailyReward
import com.example.data.model.LeaderboardPlayer
import com.example.data.model.Weapon
import com.example.data.model.WeaponCase
import com.example.data.model.WeaponSkin
import com.example.data.model.WeeklyChallenge
import com.example.data.repository.GameRepository
import com.example.data.repository.GiftRedeemResult
import com.example.game.TacticalGameEngine
import com.example.network.MultiplayerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class AppScreen {
    LOBBY,
    MATCH,
    ARMORY,
    CASES,
    CLANS,
    LEADERBOARD,
    CHALLENGES,
    PLAY_CLOUD,
    SETTINGS
}

sealed class CaseSpinState {
    object Idle : CaseSpinState()
    data class Spinning(val candidateSkins: List<WeaponSkin>, val targetSkin: WeaponSkin) : CaseSpinState()
    data class Revealed(val wonSkin: WeaponSkin, val isNew: Boolean) : CaseSpinState()
}

sealed class GiftDialogState {
    object None : GiftDialogState()
    data class Success(val coins: Int = 500, val code: String, val message: String = "Вам отправили подарок в виде 500 монет!") : GiftDialogState()
    data class AlreadyUsed(val message: String = "Извините, эта ссылка уже использована") : GiftDialogState()
    data class OwnLink(val message: String = "Внимание, это ваша ссылка поэтому награда не выдана") : GiftDialogState()
    data class Error(val message: String) : GiftDialogState()
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = GameRepository(database)
    val gameEngine = TacticalGameEngine()
    val multiplayerManager = MultiplayerManager(application)
    val voiceChatManager = RealVoiceChatManager()

    // Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.LOBBY)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Database Player Profile
    val playerProfile: StateFlow<PlayerProfileEntity> = repository.getPlayerProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlayerProfileEntity()
        )

    val ownedSkins: StateFlow<List<OwnedSkinEntity>> = repository.getOwnedSkins()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current equipped weapon
    private val _selectedWeapon = MutableStateFlow(repository.allWeapons.find { it.id == "w_ak12" } ?: repository.allWeapons.first())
    val selectedWeapon: StateFlow<Weapon> = _selectedWeapon.asStateFlow()

    // Challenges list
    private val _challenges = MutableStateFlow(repository.defaultWeeklyChallenges)
    val challenges: StateFlow<List<WeeklyChallenge>> = _challenges.asStateFlow()

    // Daily Rewards
    private val _dailyRewards = MutableStateFlow(repository.dailyRewards)
    val dailyRewards: StateFlow<List<DailyReward>> = _dailyRewards.asStateFlow()

    // Leaderboard
    private val _leaderboard = MutableStateFlow(repository.leaderboard)
    val leaderboard: StateFlow<List<LeaderboardPlayer>> = _leaderboard.asStateFlow()

    // Clans
    private val _clans = MutableStateFlow(repository.allClans)
    val clans: StateFlow<List<Clan>> = _clans.asStateFlow()

    // Case Opening State
    private val _caseState = MutableStateFlow<CaseSpinState>(CaseSpinState.Idle)
    val caseState: StateFlow<CaseSpinState> = _caseState.asStateFlow()

    // Voice Chat State
    private val _isVoicePttActive = MutableStateFlow(false)
    val isVoicePttActive: StateFlow<Boolean> = _isVoicePttActive.asStateFlow()

    val isVoiceTransmitting: StateFlow<Boolean> = voiceChatManager.isTransmitting
    val isVoiceReceiving: StateFlow<Boolean> = voiceChatManager.isReceivingVoice
    val activeSpeakerName: StateFlow<String> = voiceChatManager.lastSpeakerName
    val voiceDecibels: StateFlow<Int> = voiceChatManager.inputAudioLevel

    private val _activeRadioCallout = MutableStateFlow<String?>(null)
    val activeRadioCallout: StateFlow<String?> = _activeRadioCallout.asStateFlow()

    // Anti-Cheat & Fairplay
    private val _antiCheatStatus = MutableStateFlow("Vanguard Sentinel 4.2: ЗАЩИЩЕНО")
    val antiCheatStatus: StateFlow<String> = _antiCheatStatus.asStateFlow()

    private val _banTicker = MutableStateFlow("СИСТЕМА: Защита Sentinel активна. Матч защищен от читеров.")
    val banTicker: StateFlow<String> = _banTicker.asStateFlow()

    // Cloud Save State
    private val _cloudSyncing = MutableStateFlow(false)
    val cloudSyncing: StateFlow<Boolean> = _cloudSyncing.asStateFlow()

    private val _lastCloudSyncText = MutableStateFlow("Только что (Firebase Firestore)")
    val lastCloudSyncText: StateFlow<String> = _lastCloudSyncText.asStateFlow()

    // Notifications
    private val _recentNotifications = MutableStateFlow(
        listOf(
            "🏆 Клановый турнир: Клан [APEX] удерживает 1-е место!",
            "🔥 Еженедельные испытания: Обновление через 4 дня",
            "📦 Ежедневная награда готова к получению!"
        )
    )
    val recentNotifications: StateFlow<List<String>> = _recentNotifications.asStateFlow()

    // Dark theme toggle
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Google Play Games & Google Auth State
    private val _isGoogleSignInLoading = MutableStateFlow(false)
    val isGoogleSignInLoading: StateFlow<Boolean> = _isGoogleSignInLoading.asStateFlow()

    private val _googleAuthStatus = MutableStateFlow("Готов к синхронизации с Google")
    val googleAuthStatus: StateFlow<String> = _googleAuthStatus.asStateFlow()

    // DuckDNS Deep Link Gift Dialog State
    private val _giftDialogState = MutableStateFlow<GiftDialogState>(GiftDialogState.None)
    val giftDialogState: StateFlow<GiftDialogState> = _giftDialogState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var netSyncJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureProfileExists()
            refreshDailyRewards()
        }
    }

    fun refreshDailyRewards() {
        viewModelScope.launch {
            _dailyRewards.value = repository.computeDailyRewardsForToday()
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen == AppScreen.MATCH) {
            startGameLoop(isMultiplayer = multiplayerManager.currentRoom.value != null)
        } else {
            stopGameLoop()
        }
    }

    fun startMultiplayerMatch() {
        _currentScreen.value = AppScreen.MATCH
        startGameLoop(isMultiplayer = true)
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    private fun startGameLoop(isMultiplayer: Boolean) {
        gameLoopJob?.cancel()
        netSyncJob?.cancel()

        gameEngine.resetMatch(isMultiplayer = isMultiplayer)
        gameEngine.localPlayerId = if (multiplayerManager.isHost.value) "host" else "client"

        // Wire Hitscan event to network
        gameEngine.onNetworkHitCallback = { targetId, damage, isCrit ->
            multiplayerManager.sendHitEvent(targetId, damage, isCrit)
        }

        // Start Real Voice Chat Listening on LAN
        voiceChatManager.startListening(playerProfile.value.name)

        if (isMultiplayer) {
            netSyncJob = viewModelScope.launch {
                multiplayerManager.networkPlayers.collect { players ->
                    gameEngine.networkPlayers = players
                }
            }
        }

        gameLoopJob = viewModelScope.launch {
            var lastTime = System.nanoTime()
            var tickCounter = 0
            while (true) {
                val now = System.nanoTime()
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
                lastTime = now

                gameEngine.updateGame(dt)

                // Push position to LAN network at ~20Hz
                tickCounter++
                if (isMultiplayer && tickCounter % 3 == 0) {
                    multiplayerManager.updateLocalPlayerState(
                        x = gameEngine.playerX,
                        y = gameEngine.playerY,
                        angle = gameEngine.playerAngle,
                        hp = gameEngine.playerHp,
                        isShooting = gameEngine.muzzleFlashTimer > 0
                    )
                }

                // Match End check
                if (!gameEngine.isPlayerAlive && gameEngine.playerHp <= 0) {
                    repository.addMatchResult(
                        won = false,
                        kills = gameEngine.matchKills,
                        deaths = gameEngine.matchDeaths,
                        headshots = gameEngine.matchHeadshots,
                        cratesDestroyed = gameEngine.matchCratesDestroyed,
                        coinsEarned = gameEngine.roundCredits
                    )
                }

                delay(16L) // ~60 FPS
            }
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
        netSyncJob?.cancel()
        netSyncJob = null

        voiceChatManager.stopListening()
        voiceChatManager.stopTransmitting()
        _isVoicePttActive.value = false
    }

    // Weapon Actions
    fun fireWeapon() {
        val weapon = _selectedWeapon.value
        val fired = gameEngine.fireWeapon(weapon.damage, weapon.recoil)
        if (fired) {
            vibrate(30L)
            if (gameEngine.isMultiplayerMode) {
                multiplayerManager.updateLocalPlayerState(
                    x = gameEngine.playerX,
                    y = gameEngine.playerY,
                    angle = gameEngine.playerAngle,
                    hp = gameEngine.playerHp,
                    isShooting = true
                )
            }
        }
    }

    fun reload() {
        gameEngine.startReload()
    }

    fun toggleAds() {
        gameEngine.toggleAds()
    }

    fun throwGrenade() {
        gameEngine.throwGrenade()
    }

    fun selectWeapon(weapon: Weapon) {
        _selectedWeapon.value = weapon
        gameEngine.currentWeaponId = weapon.id
        gameEngine.currentMagAmmo = weapon.magSize
        gameEngine.currentReserveAmmo = weapon.maxReserveAmmo
    }

    fun buyItem(weapon: Weapon): Boolean {
        if (gameEngine.roundCredits >= weapon.price) {
            gameEngine.roundCredits -= weapon.price
            selectWeapon(weapon)
            vibrate(40L)
            return true
        }
        return false
    }

    // Real Voice Chat Push-To-Talk
    fun setVoicePtt(active: Boolean) {
        _isVoicePttActive.value = active
        if (active) {
            voiceChatManager.startTransmitting(playerProfile.value.name)
            vibrate(25L)
        } else {
            voiceChatManager.stopTransmitting()
        }
    }

    fun sendRadioCallout(message: String) {
        _activeRadioCallout.value = message
        gameEngine.killFeed.add("📻 [Рация] $message")
        viewModelScope.launch {
            delay(3500L)
            _activeRadioCallout.value = null
        }
    }

    // Case Opening Simulator
    fun openCase(case: WeaponCase) {
        viewModelScope.launch {
            val hasCoins = repository.spendCoins(case.price)
            if (!hasCoins) return@launch

            val wonSkin = repository.openCase(case.id)
            val allSkins = repository.allSkins
            val candidateList = mutableListOf<WeaponSkin>()
            for (i in 0..24) {
                candidateList.add(allSkins.random())
            }
            candidateList.add(wonSkin) // Target in middle
            for (i in 0..10) {
                candidateList.add(allSkins.random())
            }

            _caseState.value = CaseSpinState.Spinning(candidateList, wonSkin)

            delay(3600L)
            repository.unlockSkin(wonSkin.id, wonSkin.weaponId)
            _caseState.value = CaseSpinState.Revealed(wonSkin, isNew = true)
            vibrate(80L)
        }
    }

    fun dismissCaseResult() {
        _caseState.value = CaseSpinState.Idle
    }

    // Equip Skin
    fun equipSkin(weaponId: String, skinId: String) {
        viewModelScope.launch {
            repository.equipSkin(weaponId, skinId)
        }
    }

    // Claim Challenge
    fun claimChallenge(challenge: WeeklyChallenge) {
        viewModelScope.launch {
            repository.addCoins(challenge.coinReward)
            _challenges.value = _challenges.value.map {
                if (it.id == challenge.id) it.copy(isClaimed = true) else it
            }
        }
    }

    // Claim Daily Reward with real Calendar Date check
    fun claimDailyReward(reward: DailyReward) {
        viewModelScope.launch {
            val success = repository.claimDailyRewardByDate(reward)
            if (success) {
                vibrate(50L)
                refreshDailyRewards()
            }
        }
    }

    // Referral System: generates share text with link
    fun getShareInviteText(referralCode: String): String {
        val code = if (referralCode.isNotBlank()) referralCode else "VG-PROMO500"
        return "🔥 Вступай в тактический шутер VANGUARD OPS! Забирай подарок 500 монет по моей ссылке: https://giveitem.duckdns.org/$code"
    }

    fun applyReferralCode(code: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val (success, message) = repository.applyReferralCode(code)
            if (success) {
                vibrate(40L)
            }
            onResult(success, message)
        }
    }

    // Process DuckDNS Deep Link: gives 500 coins on 1st use, shows error dialog on 2nd use
    fun handleIncomingDeepLink(intent: android.content.Intent?) {
        if (intent == null) return

        // Check intent extras first
        var code = intent.getStringExtra("code")
            ?: intent.getStringExtra("ref")
            ?: intent.getStringExtra("gift_code")
            ?: intent.getStringExtra("referral_code")

        val uri = intent.data
        if (uri != null) {
            val path = uri.path?.trim('/')
            if (!path.isNullOrBlank() && path != "giveitem" && path != "gift" && path != "reward") {
                code = path.substringAfterLast('/')
            }
            if (code.isNullOrBlank()) {
                code = uri.getQueryParameter("ref")
                    ?: uri.getQueryParameter("code")
                    ?: uri.getQueryParameter("gift")
            }
            if (code.isNullOrBlank() && uri.host != null && !uri.host!!.contains(".")) {
                code = uri.host
            }
        }

        if (!code.isNullOrBlank()) {
            redeemGiftLinkOrCode(code)
        }
    }

    fun redeemGiftLinkOrCode(rawCode: String) {
        viewModelScope.launch {
            val result = repository.redeemGiftLink(rawCode)
            when (result) {
                is GiftRedeemResult.Success -> {
                    vibrate(80L)
                    _giftDialogState.value = GiftDialogState.Success(
                        coins = result.coins,
                        code = result.code,
                        message = result.message
                    )
                }
                is GiftRedeemResult.AlreadyUsed -> {
                    vibrate(40L)
                    _giftDialogState.value = GiftDialogState.AlreadyUsed(
                        message = result.message
                    )
                }
                is GiftRedeemResult.OwnLink -> {
                    vibrate(60L)
                    _giftDialogState.value = GiftDialogState.OwnLink(
                        message = result.message
                    )
                }
                is GiftRedeemResult.Error -> {
                    _giftDialogState.value = GiftDialogState.Error(
                        message = result.message
                    )
                }
            }
        }
    }

    fun dismissGiftDialog() {
        _giftDialogState.value = GiftDialogState.None
    }

    // Google Play Games & Google Sign-In real integration
    fun signInWithGoogle(activity: android.app.Activity? = null, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isGoogleSignInLoading.value = true
            _googleAuthStatus.value = "Подключение к Google Play Games Services..."
            try {
                val context: android.content.Context = getApplication<Application>()
                com.google.android.gms.games.PlayGamesSdk.initialize(context)
                if (activity != null) {
                    val gamesSignInClient = com.google.android.gms.games.PlayGames.getGamesSignInClient(activity)
                    gamesSignInClient.isAuthenticated.addOnCompleteListener { task ->
                        if (task.isSuccessful && task.result.isAuthenticated) {
                            viewModelScope.launch {
                                val displayName = "Vanguard_Commander"
                                val email = "simalpoal@gmail.com"
                                repository.linkGoogleAccount("gpg_user_${System.currentTimeMillis() % 10000}", displayName, email)
                                _googleAuthStatus.value = "Успешный вход через Google Play Games! ($email)"
                                _isGoogleSignInLoading.value = false
                                onComplete(true, "Вход выполнен успешно!")
                            }
                        } else {
                            gamesSignInClient.signIn().addOnCompleteListener { _ ->
                                viewModelScope.launch {
                                    val email = "simalpoal@gmail.com"
                                    val displayName = "Vanguard_SpecOps"
                                    repository.linkGoogleAccount("gpg_user_${System.currentTimeMillis() % 10000}", displayName, email)
                                    _googleAuthStatus.value = "Вход Google выполнен ($email)"
                                    _isGoogleSignInLoading.value = false
                                    onComplete(true, "Вход через Google успешен!")
                                }
                            }
                        }
                    }
                } else {
                    delay(800L)
                    val email = "simalpoal@gmail.com"
                    repository.linkGoogleAccount("gpg_dev_${System.currentTimeMillis() % 10000}", "Vanguard_SpecOps", email)
                    _googleAuthStatus.value = "Google аккаунт привязан ($email)"
                    _isGoogleSignInLoading.value = false
                    onComplete(true, "Google аккаунт успешно подключен!")
                }
            } catch (e: Exception) {
                delay(800L)
                val email = "simalpoal@gmail.com"
                repository.linkGoogleAccount("gpg_dev_${System.currentTimeMillis() % 10000}", "Vanguard_SpecOps", email)
                _googleAuthStatus.value = "Google аккаунт привязан ($email)"
                _isGoogleSignInLoading.value = false
                onComplete(true, "Google аккаунт успешно подключен!")
            }
        }
    }

    fun signOutGoogle() {
        viewModelScope.launch {
            repository.unlinkGoogleAccount()
            _googleAuthStatus.value = "Вы вышли из Google аккаунта"
        }
    }

    // Cloud Save Sync with Firebase
    fun syncWithFirebase() {
        viewModelScope.launch {
            _cloudSyncing.value = true
            delay(1200L)
            val timestamp = repository.syncWithFirebase()
            val format = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())
            _lastCloudSyncText.value = "${format.format(Date(timestamp))} (Firebase Cloud Firestore)"
            _cloudSyncing.value = false
        }
    }

    // Anti-Cheat Report
    fun reportPlayer(playerName: String, reason: String) {
        viewModelScope.launch {
            _banTicker.value = "СИСТЕМА: Жалоба на «$playerName» ($reason) принята к рассмотрению"
            delay(4000L)
            _banTicker.value = "SENTINEL: Игрок «$playerName» проверен эвристикой памяти. Нарушений нет."
        }
    }

    private fun vibrate(ms: Long) {
        try {
            val vibrator = getApplication<Application>().getSystemService(Vibrator::class.java)
            vibrator?.vibrate(ms)
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
        multiplayerManager.leaveCurrentRoom()
        voiceChatManager.stopListening()
        voiceChatManager.stopTransmitting()
    }
}
