package com.trade.monitor

import androidx.compose.runtime.*
import com.trade.monitor.theme.TradeMonitorTheme
import kotlinx.coroutines.delay

sealed class Screen {
    data object Home : Screen()
    data object Forex : Screen()
    data object Crypto : Screen()
}

@Composable
fun App() {
    TradeMonitorTheme {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
        val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }

        fun navigateTo(screen: Screen) {
            backStack.add(screen)
            currentScreen = screen
        }

        fun goBack() {
            if (backStack.size > 1) {
                backStack.removeLast()
                currentScreen = backStack.last()
            }
        }

        when (currentScreen) {
            is Screen.Home -> HomeScreen(
                onForexClick = { navigateTo(Screen.Forex) },
                onCryptoClick = { navigateTo(Screen.Crypto) }
            )
            is Screen.Forex -> ForexScreen(onBack = { goBack() })
            is Screen.Crypto -> CryptoScreen(onBack = { goBack() })
        }
    }
}
