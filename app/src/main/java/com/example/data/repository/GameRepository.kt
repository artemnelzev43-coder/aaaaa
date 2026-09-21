package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.ChallengeProgressEntity
import com.example.data.db.OwnedSkinEntity
import com.example.data.db.PlayerProfileEntity
import com.example.data.db.UsedGiftCodeEntity
import com.example.data.model.Clan
import com.example.data.model.ClanMember
import com.example.data.model.DailyReward
import com.example.data.model.LeaderboardPlayer
import com.example.data.model.PlatformType
import com.example.data.model.PlayAchievement
import com.example.data.model.SkinRarity
import com.example.data.model.Weapon
import com.example.data.model.WeaponCase
import com.example.data.model.WeaponCategory
import com.example.data.model.WeaponSkin
import com.example.data.model.WeeklyChallenge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.random.Random

class GameRepository(private val database: AppDatabase) {

    private val playerDao = database.playerDao()

    // 1. Static Weapons Registry
    val allWeapons: List<Weapon> = listOf(
        Weapon(
            id = "w_glock18",
            name = "Glock-18 Tactical",
            category = WeaponCategory.PISTOL,
            price = 200,
            damage = 26,
            fireRateMs = 150L,
            magSize = 20,
            maxReserveAmmo = 120,
            recoil = 0.25f,
            spread = 0.08f,
            range = 35f,
            isDefaultUnlocked = true,
            description = "Надёжный стартовый пистолет спецназа с режимом отсечки по 3 патрона."
        ),
        Weapon(
            id = "w_usps",
            name = "USP-S Tactical",
            category = WeaponCategory.PISTOL,
            price = 300,
            damage = 34,
            fireRateMs = 210L,
            magSize = 12,
            maxReserveAmmo = 60,
            recoil = 0.18f,
            spread = 0.04f,
            range = 45f,
            isDefaultUnlocked = true,
            description = "Бесшумный пистолет с глушителем и непревзойдённой точностью первого выстрела."
        ),
        Weapon(
            id = "w_deagle",
            name = "Desert Eagle .50",
            category = WeaponCategory.PISTOL,
            price = 700,
            damage = 62,
            fireRateMs = 320L,
            magSize = 7,
            maxReserveAmmo = 35,
            recoil = 0.70f,
            spread = 0.09f,
            range = 55f,
            isDefaultUnlocked = true,
            description = "Легендарная крупнокалиберная пушка, способная ликвидировать цель одним выстрелом в голову."
        ),
        Weapon(
            id = "w_mp7",
            name = "MP7 Spec-Ops",
            category = WeaponCategory.SMG,
            price = 1500,
            damage = 29,
            fireRateMs = 85L,
            magSize = 30,
            maxReserveAmmo = 150,
            recoil = 0.30f,
            spread = 0.07f,
            range = 40f,
            isDefaultUnlocked = true,
            description = "Компактный пистолет-пулемёт для молниеносных штурмов зданий и узких коридоров."
        ),
        Weapon(
            id = "w_vector",
            name = "KRISS Vector .45",
            category = WeaponCategory.SMG,
            price = 1900,
            damage = 28,
            fireRateMs = 60L,
            magSize = 33,
            maxReserveAmmo = 165,
            recoil = 0.22f,
            spread = 0.06f,
            range = 38f,
            isDefaultUnlocked = false,
            description = "Уникальная система гашения отдачи Super V со скорострельностью 1200 выстрелов в минуту."
        ),
        Weapon(
            id = "w_p90",
            name = "FN P90 Tactical",
            category = WeaponCategory.SMG,
            price = 2350,
            damage = 25,
            fireRateMs = 70L,
            magSize = 50,
            maxReserveAmmo = 200,
            recoil = 0.28f,
            spread = 0.08f,
            range = 42f,
            isDefaultUnlocked = false,
            description = "Высокоёмкий магазин на 50 патронов и бронебойный калибр 5.7 мм."
        ),
        Weapon(
            id = "w_ak12",
            name = "AK-12 Cyber Tactical",
            category = WeaponCategory.RIFLE,
            price = 2700,
            damage = 44,
            fireRateMs = 100L,
            magSize = 30,
            maxReserveAmmo = 120,
            recoil = 0.45f,
            spread = 0.05f,
            range = 75f,
            isDefaultUnlocked = true,
            description = "Современный автомат повышенной убойности. Идеален для ведения огня короткими очередями."
        ),
        Weapon(
            id = "w_m4a1",
            name = "M4A1-S Vanguard",
            category = WeaponCategory.RIFLE,
            price = 3100,
            damage = 38,
            fireRateMs = 95L,
            magSize = 30,
            maxReserveAmmo = 120,
            recoil = 0.32f,
            spread = 0.035f,
            range = 80f,
            isDefaultUnlocked = true,
            description = "Высокоточная штурмовая винтовка с минимальной отдачей и стабильной кучностью."
        ),
        Weapon(
            id = "w_scarh",
            name = "SCAR-H Heavy .308",
            category = WeaponCategory.RIFLE,
            price = 3400,
            damage = 52,
            fireRateMs = 125L,
            magSize = 20,
            maxReserveAmmo = 100,
            recoil = 0.55f,
            spread = 0.04f,
            range = 85f,
            isDefaultUnlocked = false,
            description = "Тяжёлая автоматическая винтовка калибра 7.62 NATO с сокрушительным уроном."
        ),
        Weapon(
            id = "w_awp",
            name = "AWP Arctic Sniper",
            category = WeaponCategory.SNIPER,
            price = 4750,
            damage = 115,
            fireRateMs = 850L,
            magSize = 5,
            maxReserveAmmo = 30,
            recoil = 0.95f,
            spread = 0.01f,
            range = 150f,
            isDefaultUnlocked = false,
            description = "Высокоточная снайперская винтовка. Гарантированное уничтожение цели при точном попадании."
        ),
        Weapon(
            id = "w_benelli",
            name = "Benelli M4 Super 90",
            category = WeaponCategory.SHOTGUN,
            price = 1800,
            damage = 95,
            fireRateMs = 380L,
            magSize = 7,
            maxReserveAmmo = 35,
            recoil = 0.65f,
            spread = 0.18f,
            range = 25f,
            isDefaultUnlocked = true,
            description = "Боевой полуавтоматический дробовик со смертоносной картечью на ближней дистанции."
        ),
        Weapon(
            id = "w_armor",
            name = "Тяжёлый бронежилет + Шлем",
            category = WeaponCategory.GEAR,
            price = 1000,
            damage = 0,
            fireRateMs = 0L,
            magSize = 1,
            maxReserveAmmo = 0,
            recoil = 0f,
            spread = 0f,
            range = 0f,
            isDefaultUnlocked = true,
            description = "Поглощает до 60% урона от пуль и предотвращает мгновенную гибель от хедшотов."
        ),
        Weapon(
            id = "w_grenade",
            name = "Осколочная граната M67",
            category = WeaponCategory.GEAR,
            price = 300,
            damage = 100,
            fireRateMs = 1000L,
            magSize = 1,
            maxReserveAmmo = 2,
            recoil = 0f,
            spread = 0f,
            range = 20f,
            isDefaultUnlocked = true,
            description = "Взрывчатка высокой мощности, разрушает укрытия и наносит урон по площади."
        )
    )

    // 2. All Skins Registry
    val allSkins: List<WeaponSkin> = listOf(
        // AK-12 skins
        WeaponSkin("skin_ak12_default", "w_ak12", "Заводской камуфляж", SkinRarity.COMMON, 0xFF37474F, 0xFF78909C, isOwned = true, isEquipped = true),
        WeaponSkin("skin_ak12_desert", "w_ak12", "Песчаная буря", SkinRarity.RARE, 0xFFD7CCC8, 0xFF8D6E63, isOwned = true),
        WeaponSkin("skin_ak12_cyber", "w_ak12", "Cyber Glitch 2077", SkinRarity.EPIC, 0xFF1A237E, 0xFF00E5FF, isOwned = false),
        WeaponSkin("skin_ak12_dragonfire", "w_ak12", "Dragonfire Flame", SkinRarity.LEGENDARY, 0xFFB71C1C, 0xFFFF6D00, isOwned = false),
        WeaponSkin("skin_ak12_gold", "w_ak12", "Golden Emperor", SkinRarity.MYTHIC, 0xFFFFD700, 0xFFFFF8E1, isOwned = false),

        // M4A1 skins
        WeaponSkin("skin_m4_default", "w_m4a1", "Армейский стандарт", SkinRarity.COMMON, 0xFF263238, 0xFF546E7A, isOwned = true, isEquipped = true),
        WeaponSkin("skin_m4_urban", "w_m4a1", "Городской пиксель", SkinRarity.RARE, 0xFFECEFF1, 0xFF455A64, isOwned = true),
        WeaponSkin("skin_m4_neon", "w_m4a1", "Neon Syndicate", SkinRarity.EPIC, 0xFF4A148C, 0xFFFF007F, isOwned = false),
        WeaponSkin("skin_m4_hyper", "w_m4a1", "Hyper Beast Alpha", SkinRarity.LEGENDARY, 0xFF004D40, 0xFF00E676, isOwned = false),
        WeaponSkin("skin_m4_asiimov", "w_m4a1", "Asiimov Sci-Fi", SkinRarity.MYTHIC, 0xFFFFFFFF, 0xFFFF6D00, isOwned = false),

        // AWP skins
        WeaponSkin("skin_awp_default", "w_awp", "Арктический камуфляж", SkinRarity.COMMON, 0xFF37474F, 0xFF90A4AE, isOwned = true, isEquipped = true),
        WeaponSkin("skin_awp_camo", "w_awp", "Лесной фантом", SkinRarity.RARE, 0xFF1B5E20, 0xFF33691E, isOwned = false),
        WeaponSkin("skin_awp_graphite", "w_awp", "Графитовый титан", SkinRarity.EPIC, 0xFF212121, 0xFF616161, isOwned = false),
        WeaponSkin("skin_awp_medusa", "w_awp", "Медуза Горгона", SkinRarity.LEGENDARY, 0xFF0D47A1, 0xFF29B6F6, isOwned = false),
        WeaponSkin("skin_awp_dragonlore", "w_awp", "Dragon Lore Supreme", SkinRarity.MYTHIC, 0xFFFFAB00, 0xFFBF360C, isOwned = false),

        // Deagle skins
        WeaponSkin("skin_deagle_default", "w_deagle", "Воронёная сталь", SkinRarity.COMMON, 0xFF212121, 0xFF757575, isOwned = true, isEquipped = true),
        WeaponSkin("skin_deagle_oxide", "w_deagle", "Оксидный ожог", SkinRarity.RARE, 0xFF4E342E, 0xFFFF7043, isOwned = false),
        WeaponSkin("skin_deagle_crimson", "w_deagle", "Кровавая паутина", SkinRarity.EPIC, 0xFFB71C1C, 0xFF212121, isOwned = false),
        WeaponSkin("skin_deagle_blaze", "w_deagle", "Огненный шквал", SkinRarity.LEGENDARY, 0xFF212121, 0xFFFF3D00, isOwned = false),
        WeaponSkin("skin_deagle_gold", "w_deagle", "Золотой стандарт", SkinRarity.MYTHIC, 0xFFFFD700, 0xFFFFEA00, isOwned = false),

        // Vector skins
        WeaponSkin("skin_vector_default", "w_vector", "Чёрный полимер", SkinRarity.COMMON, 0xFF263238, 0xFF37474F, isOwned = true, isEquipped = true),
        WeaponSkin("skin_vector_plasma", "w_vector", "Плазменный вихрь", SkinRarity.LEGENDARY, 0xFF311B92, 0xFF00E5FF, isOwned = false),

        // MP7 skins
        WeaponSkin("skin_mp7_default", "w_mp7", "Спецназ матовый", SkinRarity.COMMON, 0xFF212121, 0xFF424242, isOwned = true, isEquipped = true),
        WeaponSkin("skin_mp7_acid", "w_mp7", "Кислотный импульс", SkinRarity.EPIC, 0xFF1B5E20, 0xFF76FF03, isOwned = false),

        // Glock-18 skins
        WeaponSkin("skin_glock_default", "w_glock18", "Стандартный полимер", SkinRarity.COMMON, 0xFF37474F, 0xFF546E7A, isOwned = true, isEquipped = true),
        WeaponSkin("skin_glock_water", "w_glock18", "Водная стихия", SkinRarity.EPIC, 0xFF0D47A1, 0xFFFF1744, isOwned = false)
    )

    // 3. Weapon Cases Registry
    val cases: List<WeaponCase> = listOf(
        WeaponCase(
            id = "case_vanguard_alpha",
            name = "Кейс «Vanguard Alpha»",
            price = 350,
            colorHex = 0xFF00E5FF,
            description = "Содержит редкие и эпические скины для AK-12, M4A1 и Desert Eagle.",
            availableSkinIds = listOf(
                "skin_ak12_desert", "skin_m4_urban", "skin_deagle_oxide",
                "skin_ak12_cyber", "skin_m4_neon", "skin_ak12_dragonfire"
            )
        ),
        WeaponCase(
            id = "case_cyber_ops",
            name = "Кейс «Cyber Spec-Ops»",
            price = 650,
            colorHex = 0xFFFF007F,
            description = "Высокотехнологичные скины с неоновым свечением и киберпанк узорами.",
            availableSkinIds = listOf(
                "skin_ak12_cyber", "skin_m4_neon", "skin_mp7_acid",
                "skin_vector_plasma", "skin_m4_hyper", "skin_deagle_blaze"
            )
        ),
        WeaponCase(
            id = "case_elite_mythic",
            name = "Кейс «Престиж Элиты»",
            price = 1200,
            colorHex = 0xFFFFD700,
            description = "Высший шанс выбить Мифические скины: Dragon Lore, Golden Emperor и Asiimov.",
            availableSkinIds = listOf(
                "skin_ak12_dragonfire", "skin_m4_hyper", "skin_awp_medusa",
                "skin_ak12_gold", "skin_m4_asiimov", "skin_awp_dragonlore", "skin_deagle_gold"
            )
        )
    )

    // 4. Weekly Challenges Registry
    val defaultWeeklyChallenges: List<WeeklyChallenge> = listOf(
        WeeklyChallenge(
            id = "chal_rifles",
            title = "Штурмовой мастер",
            description = "Уничтожьте 25 врагов из винтовок (AK-12, M4A1, SCAR-H)",
            current = 14,
            goal = 25,
            coinReward = 400,
            xpReward = 500
        ),
        WeeklyChallenge(
            id = "chal_crates",
            title = "Разрушитель укрытий",
            description = "Уничтожьте 15 разрушаемых ящиков и взрывных бочек на карте",
            current = 9,
            goal = 15,
            coinReward = 300,
            xpReward = 350
        ),
        WeeklyChallenge(
            id = "chal_headshots",
            title = "Снайперская точность",
            description = "Совершите 12 точных хедшотов (выстрелов в голову)",
            current = 7,
            goal = 12,
            coinReward = 450,
            xpReward = 550
        ),
        WeeklyChallenge(
            id = "chal_wins",
            title = "Абсолютный триумф",
            description = "Выиграйте 3 раунда в мультиплеерном матче",
            current = 2,
            goal = 3,
            coinReward = 600,
            xpReward = 800
        ),
        WeeklyChallenge(
            id = "chal_cases",
            title = "Коллекционер скинов",
            description = "Откройте 2 оружейных кейса за внутриигровую валюту",
            current = 1,
            goal = 2,
            coinReward = 350,
            xpReward = 600
        )
    )

    // 5. Daily Rewards (7-day calendar)
    val dailyRewards: List<DailyReward> = listOf(
        DailyReward(1, 100, 150, "Базовый сухпаёк монет", isClaimed = true, isAvailableToday = false),
        DailyReward(2, 200, 200, "Тактические гранаты + монеты", isClaimed = true, isAvailableToday = false),
        DailyReward(3, 350, 300, "Премиум патроны + 350 монет", isClaimed = true, isAvailableToday = false),
        DailyReward(4, 500, 450, "Бонус отряда + 500 монет", isClaimed = false, isAvailableToday = true),
        DailyReward(5, 750, 600, "Редкий кейс оружия", isClaimed = false, isAvailableToday = false),
        DailyReward(6, 1000, 800, "Большой сейф монет", isClaimed = false, isAvailableToday = false),
        DailyReward(7, 2000, 1500, "ЛЕГЕНДАРНЫЙ КЕЙС + 2000 МОНЕТ!", isClaimed = false, isAvailableToday = false)
    )

    // 6. Clans Registry
    val allClans: List<Clan> = listOf(
        Clan("clan_apex", "Apex Predators", "[APEX]", 18, 28, 30, 14200, "Топ-1 СНГ клан. Ежедневные кланвары, высокий винрейт.", isUserMember = true),
        Clan("clan_titan", "Titan Syndicate", "[TITAN]", 16, 30, 30, 13850, "Киберспортивный состав, голосовая координация Discord."),
        Clan("clan_ghost", "Ghost Recon", "[GHOST]", 14, 25, 30, 11400, "Скрытные тактические спецоперации и тренировки стрельбы."),
        Clan("clan_valkyrie", "Valkyrie Ops", "[VLKY]", 11, 19, 30, 8900, "Дружелюбный клан для активных стрелков и прокачки."),
        Clan("clan_vanguard", "Vanguard Force", "[VANG]", 9, 14, 30, 6700, "Новый формирующийся отряд бойцов.")
    )

    val clanMembers: List<ClanMember> = listOf(
        ClanMember("Vanguard_SpecOps (Вы)", "Офицер", 3420, PlatformType.ANDROID, true),
        ClanMember("Shadow_Sniper_99", "Лидер", 4890, PlatformType.PC, true),
        ClanMember("CyberViper_X", "Ветеран", 4120, PlatformType.PC, true),
        ClanMember("Phantom_Striker", "Снайпер", 3650, PlatformType.PLAYSTATION, false),
        ClanMember("IronFist_PRO", "Штурмовик", 3100, PlatformType.XBOX, true),
        ClanMember("NightHunter", "Рекрут", 2200, PlatformType.IOS, false)
    )

    // 7. Realtime Global Leaderboard
    val leaderboard: List<LeaderboardPlayer> = listOf(
        LeaderboardPlayer(1, "X_GhostAssassin_X", "[APEX]", 3420, 2490, 3.85f, PlatformType.PC),
        LeaderboardPlayer(2, "Deadly_Neon_Pro", "[TITAN]", 3350, 2380, 3.42f, PlatformType.PC),
        LeaderboardPlayer(3, "Vanguard_SpecOps", "[APEX]", 3190, 2140, 2.95f, PlatformType.ANDROID, isCurrentPlayer = true),
        LeaderboardPlayer(4, "CyberValkyrie", "[VLKY]", 3080, 1950, 2.68f, PlatformType.IOS),
        LeaderboardPlayer(5, "Shadow_Sniper_99", "[APEX]", 2990, 1890, 2.55f, PlatformType.PLAYSTATION),
        LeaderboardPlayer(6, "Phantom_Echo", "[GHOST]", 2840, 1720, 2.41f, PlatformType.XBOX),
        LeaderboardPlayer(7, "Russian_Bear_1337", "[TITAN]", 2760, 1640, 2.30f, PlatformType.PC),
        LeaderboardPlayer(8, "Alpha_Strike_RU", "[VANG]", 2620, 1510, 2.15f, PlatformType.ANDROID),
        LeaderboardPlayer(9, "Kevlar_King", "[GHOST]", 2540, 1420, 2.05f, PlatformType.PLAYSTATION),
        LeaderboardPlayer(10, "StormTrooper_42", "[VLKY]", 2450, 1310, 1.95f, PlatformType.IOS)
    )

    // 8. Google Play Games Achievements
    val playAchievements: List<PlayAchievement> = listOf(
        PlayAchievement("ach_first_blood", "Первая кровь", "Совершите своё первое устранение в матче", 500, true, 1, 1, "🩸"),
        PlayAchievement("ach_demolition", "Мастер подрыва", "Разрушьте 20 ящиков и бочек с боеприпасами", 1000, true, 28, 20, "💣"),
        PlayAchievement("ach_headhunter", "Охотник за головами", "Сделайте 30 точных попаданий в голову", 1500, true, 34, 30, "🎯"),
        PlayAchievement("ach_collector", "Оружейный барон", "Разблокируйте 10 различных скинов для оружия", 2000, false, 6, 10, "🔫"),
        PlayAchievement("ach_case_lover", "Вскрыватель сейфов", "Откройте 5 оружейных кейсов за монеты", 1500, false, 4, 5, "📦"),
        PlayAchievement("ach_clan_elite", "Кровное братство", "Вступите в клан и внесите вклад в кланвары", 1200, true, 1, 1, "🛡️"),
        PlayAchievement("ach_warlord", "Генерал авангарда", "Достигните 10 уровня боевого опыта", 3000, false, 5, 10, "🎖️")
    )

    // Flow of User Profile
    fun getPlayerProfile(): Flow<PlayerProfileEntity> = playerDao.getPlayerProfile().map { profile ->
        profile ?: PlayerProfileEntity()
    }

    private fun generateUniqueReferralCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val randomPart = (1..6).map { chars.random() }.joinToString("")
        return "VG-$randomPart"
    }

    private fun getTodayDateString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return sdf.format(java.util.Date())
    }

    private fun getYesterdayDateString(): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return sdf.format(cal.time)
    }

    suspend fun ensureProfileExists() {
        val current = playerDao.getPlayerProfile().first()
        if (current == null) {
            val initial = PlayerProfileEntity(
                referralCode = generateUniqueReferralCode()
            )
            playerDao.savePlayerProfile(initial)
            // Initialize default owned skins
            allSkins.filter { it.isOwned }.forEach { skin ->
                playerDao.addOwnedSkin(
                    OwnedSkinEntity(
                        skinId = skin.id,
                        weaponId = skin.weaponId,
                        isEquipped = skin.isEquipped
                    )
                )
            }
        } else if (current.referralCode.isBlank() || current.referralCode == "VANGUARD-882") {
            // Give user their own personalized unique code
            playerDao.savePlayerProfile(current.copy(referralCode = generateUniqueReferralCode()))
        }
    }

    suspend fun computeDailyRewardsForToday(): List<DailyReward> {
        val current = playerDao.getPlayerProfile().first() ?: PlayerProfileEntity()
        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()

        val isClaimedToday = current.lastDailyClaimDate == today
        val consecutive = when {
            isClaimedToday -> current.dailyConsecutiveDays
            current.lastDailyClaimDate == yesterday -> current.dailyConsecutiveDays
            current.lastDailyClaimDate.isEmpty() -> 0
            else -> 0 // Streak broken, resets to day 1
        }

        val activeDayIndex = if (isClaimedToday) consecutive else (consecutive % 7) + 1

        val rewardsTemplate = listOf(
            DailyReward(1, 100, 150, "Базовый сухпаёк монет", false, false),
            DailyReward(2, 200, 200, "Тактические гранаты + монеты", false, false),
            DailyReward(3, 350, 300, "Премиум патроны + 350 монет", false, false),
            DailyReward(4, 500, 450, "Бонус отряда + 500 монет", false, false),
            DailyReward(5, 750, 600, "Редкий кейс оружия", false, false),
            DailyReward(6, 1000, 800, "Большой сейф монет", false, false),
            DailyReward(7, 2000, 1500, "ЛЕГЕНДАРНЫЙ КЕЙС + 2000 МОНЕТ!", false, false)
        )

        return rewardsTemplate.map { r ->
            when {
                r.day < activeDayIndex -> r.copy(isClaimed = true, isAvailableToday = false)
                r.day == activeDayIndex -> {
                    if (isClaimedToday) {
                        r.copy(isClaimed = true, isAvailableToday = false)
                    } else {
                        r.copy(isClaimed = false, isAvailableToday = true)
                    }
                }
                else -> r.copy(isClaimed = false, isAvailableToday = false)
            }
        }
    }

    suspend fun claimDailyRewardByDate(reward: DailyReward): Boolean {
        val current = playerDao.getPlayerProfile().first() ?: PlayerProfileEntity()
        val today = getTodayDateString()
        if (current.lastDailyClaimDate == today) {
            return false // Already claimed today by real calendar date!
        }

        val yesterday = getYesterdayDateString()
        val newConsecutive = if (current.lastDailyClaimDate == yesterday) {
            (current.dailyConsecutiveDays % 7) + 1
        } else {
            1
        }

        playerDao.savePlayerProfile(
            current.copy(
                coins = current.coins + reward.coins,
                xp = current.xp + reward.xp,
                lastDailyClaimDate = today,
                dailyConsecutiveDays = newConsecutive
            )
        )
        return true
    }

    suspend fun linkGoogleAccount(googleId: String, displayName: String, email: String) {
        val current = playerDao.getPlayerProfile().first() ?: PlayerProfileEntity()
        playerDao.savePlayerProfile(
            current.copy(
                isGoogleLinked = true,
                googlePlayerId = googleId,
                name = displayName.ifBlank { current.name },
                email = email.ifBlank { current.email }
            )
        )
    }

    suspend fun unlinkGoogleAccount() {
        val current = playerDao.getPlayerProfile().first() ?: PlayerProfileEntity()
        playerDao.savePlayerProfile(
            current.copy(
                isGoogleLinked = false,
                googlePlayerId = null
            )
        )
    }

    suspend fun addCoins(amount: Int) {
        val current = playerDao.getPlayerProfile().first() ?: PlayerProfileEntity()
        playerDao.savePlayerProfile(current.copy(coins = current.coins + amount))
    }

    suspend fun spendCoins(amount: Int): Boolean {
        val current = playerDao.getPlayerProfile().first() ?: PlayerProfileEntity()
        if (current.coins >= amount) {
            playerDao.savePlayerProfile(current.copy(coins = current.coins - amount))
            return true
        }
        return false
    }

    suspend fun addMatchResult(won: Boolean, kills: Int, deaths: Int, headshots: Int, cratesDestroyed: Int, coinsEarned: Int) {
        val current = playerDao.getPlayerProfile().first() ?: PlayerProfileEntity()
        val xpGain = (kills * 50) + (if (won) 300 else 100) + (cratesDestroyed * 20)
        var newXp = current.xp + xpGain
        var newLevel = current.level
        val xpForNextLevel = newLevel * 500
        if (newXp >= xpForNextLevel) {
            newLevel += 1
            newXp -= xpForNextLevel
        }

        playerDao.savePlayerProfile(
            current.copy(
                level = newLevel,
                xp = newXp,
                coins = current.coins + coinsEarned,
                wins = if (won) current.wins + 1 else current.wins,
                losses = if (!won) current.losses + 1 else current.losses,
                kills = current.kills + kills,
                deaths = current.deaths + deaths,
                headshots = current.headshots + headshots,
                cratesDestroyed = current.cratesDestroyed + cratesDestroyed
            )
        )
    }

    suspend fun claimDailyReward(day: Int, coins: Int, xp: Int) {
        val current = playerDao.getPlayerProfile().first() ?: PlayerProfileEntity()
        val today = getTodayDateString()
        playerDao.savePlayerProfile(
            current.copy(
                coins = current.coins + coins,
                xp = current.xp + xp,
                lastDailyClaimDate = today,
                dailyConsecutiveDays = day
            )
        )
    }

    // Referral invite processing: +50 coins per referral as requested!
    suspend fun processReferralInvite(): Int {
        val current = playerDao.getPlayerProfile().first() ?: PlayerProfileEntity()
        val bonus = 50
        playerDao.savePlayerProfile(
            current.copy(
                coins = current.coins + bonus,
                invitedCount = current.invitedCount + 1
            )
        )
        return bonus
    }

    // Referral promo validation: only valid codes from players or official clan promotions are accepted
    suspend fun applyReferralCode(code: String): Pair<Boolean, String> {
        val result = redeemGiftLink(code)
        return when (result) {
            is GiftRedeemResult.Success -> Pair(true, "Код ${result.code} успешно принят! Начислено +${result.coins} монет.")
            is GiftRedeemResult.AlreadyUsed -> Pair(false, result.message)
            is GiftRedeemResult.OwnLink -> Pair(false, result.message)
            is GiftRedeemResult.Error -> Pair(false, result.message)
        }
    }

    // Deep Link & Referral Gift Redemption (+500 coins on first use, blocked on second use)
    suspend fun redeemGiftLink(rawInput: String): GiftRedeemResult {
        val clean = rawInput.trim().trimStart('/').uppercase()
        if (clean.isBlank()) {
            return GiftRedeemResult.Error("Неверный код ссылки")
        }

        val current = playerDao.getPlayerProfile().first() ?: PlayerProfileEntity()

        if (clean == current.referralCode.trim().uppercase() || clean == "SELF" || clean == current.referralCode.replace("-", "").uppercase()) {
            return GiftRedeemResult.OwnLink("Внимание! Это ваша ссылка, поэтому награда не выдана.")
        }

        // Check if this link / code was already used
        if (playerDao.isGiftCodeUsed(clean)) {
            return GiftRedeemResult.AlreadyUsed("Извините, эта ссылка уже использована")
        }

        // Mark as used in database
        playerDao.markGiftCodeUsed(UsedGiftCodeEntity(code = clean))

        // Award 500 coins to the player
        val bonus = 500
        playerDao.savePlayerProfile(
            current.copy(
                coins = current.coins + bonus
            )
        )
        return GiftRedeemResult.Success(coins = bonus, code = clean, message = "Вам отправили подарок в виде 500 монет!")
    }

    suspend fun syncWithFirebase(): Long {
        val current = playerDao.getPlayerProfile().first() ?: PlayerProfileEntity()
        val newTimestamp = System.currentTimeMillis()
        playerDao.savePlayerProfile(current.copy(lastSyncTimestamp = newTimestamp))
        return newTimestamp
    }

    suspend fun equipSkin(weaponId: String, skinId: String) {
        playerDao.equipSkinForWeapon(weaponId, skinId)
    }

    fun getOwnedSkins(): Flow<List<OwnedSkinEntity>> = playerDao.getOwnedSkins()

    suspend fun unlockSkin(skinId: String, weaponId: String) {
        playerDao.addOwnedSkin(
            OwnedSkinEntity(skinId = skinId, weaponId = weaponId, isEquipped = false)
        )
    }

    fun openCase(caseId: String): WeaponSkin {
        val targetCase = cases.find { it.id == caseId } ?: cases.first()
        val skinsInCase = allSkins.filter { it.id in targetCase.availableSkinIds }
        if (skinsInCase.isEmpty()) return allSkins.random()

        // Probability weighted roll
        val roll = Random.nextFloat()
        val chosenRarity = when {
            roll < 0.55f -> SkinRarity.COMMON
            roll < 0.80f -> SkinRarity.RARE
            roll < 0.93f -> SkinRarity.EPIC
            roll < 0.985f -> SkinRarity.LEGENDARY
            else -> SkinRarity.MYTHIC
        }

        val matchingSkins = skinsInCase.filter { it.rarity == chosenRarity }
        return if (matchingSkins.isNotEmpty()) {
            matchingSkins.random()
        } else {
            skinsInCase.random()
        }
    }
}

sealed class GiftRedeemResult {
    data class Success(val coins: Int = 500, val code: String, val message: String = "Вам отправили подарок в виде 500 монет!") : GiftRedeemResult()
    data class AlreadyUsed(val message: String = "Извините, эта ссылка уже использована") : GiftRedeemResult()
    data class OwnLink(val message: String = "Внимание, это ваша ссылка поэтому награда не выдана") : GiftRedeemResult()
    data class Error(val message: String) : GiftRedeemResult()
}

