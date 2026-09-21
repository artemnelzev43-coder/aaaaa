package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GiftRewardDialog
import com.example.ui.screens.ArmoryScreen
import com.example.ui.screens.CasesScreen
import com.example.ui.screens.ChallengesScreen
import com.example.ui.screens.ClansScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.PlayCloudScreen
import com.example.ui.screens.SettingsReferralScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.handleIncomingDeepLink(intent)
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleIncomingDeepLink(intent)
    }
}

data class NavTabItem(
    val screen: AppScreen,
    val title: String,
    val icon: ImageVector
)

@Composable
fun MainAppContent(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    if (currentScreen == AppScreen.MATCH) {
        // Fullscreen tactical immersive view during battle
        GameScreen(viewModel = viewModel)
    } else {
        val navTabs = listOf(
            NavTabItem(AppScreen.LOBBY, "Лобби", Icons.Default.Home),
            NavTabItem(AppScreen.ARMORY, "Арсенал", Icons.Default.MilitaryTech),
            NavTabItem(AppScreen.CASES, "Кейсы", Icons.Default.CardGiftcard),
            NavTabItem(AppScreen.CLANS, "Кланы", Icons.Default.Shield),
            NavTabItem(AppScreen.LEADERBOARD, "Топ", Icons.Default.EmojiEvents),
            NavTabItem(AppScreen.CHALLENGES, "Задания", Icons.Default.Assignment),
            NavTabItem(AppScreen.PLAY_CLOUD, "Play & Cloud", Icons.Default.SportsEsports),
            NavTabItem(AppScreen.SETTINGS, "Настройки", Icons.Default.Settings)
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color(0xFF070A0F),
            bottomBar = {
                Surface(
                    color = Color(0xFF0C121D),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth().height(64.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        navTabs.forEach { tab ->
                            val isSelected = currentScreen == tab.screen
                            Surface(
                                onClick = { viewModel.navigateTo(tab.screen) },
                                color = if (isSelected) Color(0xFF16253B) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                                    .testTag("nav_tab_${tab.screen.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) Color(0xFF00E5FF) else Color(0xFF64748B),
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Text(
                                        text = tab.title,
                                        color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF94A3B8),
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    AppScreen.LOBBY -> LobbyScreen(viewModel = viewModel)
                    AppScreen.ARMORY -> ArmoryScreen(viewModel = viewModel)
                    AppScreen.CASES -> CasesScreen(viewModel = viewModel)
                    AppScreen.CLANS -> ClansScreen(viewModel = viewModel)
                    AppScreen.LEADERBOARD -> LeaderboardScreen(viewModel = viewModel)
                    AppScreen.CHALLENGES -> ChallengesScreen(viewModel = viewModel)
                    AppScreen.PLAY_CLOUD -> PlayCloudScreen(viewModel = viewModel)
                    AppScreen.SETTINGS -> SettingsReferralScreen(viewModel = viewModel)
                    AppScreen.MATCH -> {} // Handled above
                }
            }
        }
    }

    // Global Gift & Referral Reward Dialog (500 Coins or Already Used)
    val giftDialogState by viewModel.giftDialogState.collectAsState()
    GiftRewardDialog(
        state = giftDialogState,
        onDismiss = { viewModel.dismissGiftDialog() }
    )
}
