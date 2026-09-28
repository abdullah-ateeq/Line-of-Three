package com.example.lineofthree.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lineofthree.data.AppSettings
import com.example.lineofthree.ui.screens.AboutScreen
import com.example.lineofthree.ui.screens.GameScreen
import com.example.lineofthree.ui.screens.HomeScreen
import com.example.lineofthree.ui.screens.HowToPlayScreen
import com.example.lineofthree.ui.screens.ResultScreen
import com.example.lineofthree.ui.screens.SettingsScreen
import com.example.lineofthree.ui.screens.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val GAME = "game"
    const val RESULT = "result"
    const val HOW_TO_PLAY = "how_to_play"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}

/** Ignores repeated taps once a screen has started leaving. */
private val NavBackStackEntry.isResumed: Boolean
    get() = lifecycle.currentState == Lifecycle.State.RESUMED

private fun NavHostController.navigateFrom(entry: NavBackStackEntry, route: String) {
    if (entry.isResumed) navigate(route) { launchSingleTop = true }
}

private fun NavHostController.backFrom(entry: NavBackStackEntry) {
    if (entry.isResumed) popBackStack()
}

@Composable
fun AppNavigation(appViewModel: AppViewModel, settings: AppSettings) {
    val nav = rememberNavController()
    val gameViewModel: GameViewModel = viewModel(factory = AppViewModel.Factory)
    val stats by appViewModel.statistics.collectAsStateWithLifecycle()

    NavHost(
        navController = nav,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(260)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(260)) },
        popExitTransition = { fadeOut(tween(200)) }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                nav.navigate(Routes.HOME) { popUpTo(Routes.SPLASH) { inclusive = true } }
            })
        }
        composable(Routes.HOME) { entry ->
            HomeScreen(
                stats = stats,
                onPlay = {
                    if (entry.isResumed) {
                        gameViewModel.newMatch()
                        nav.navigate(Routes.GAME) { launchSingleTop = true }
                    }
                },
                onHowToPlay = { nav.navigateFrom(entry, Routes.HOW_TO_PLAY) },
                onSettings = { nav.navigateFrom(entry, Routes.SETTINGS) },
                onAbout = { nav.navigateFrom(entry, Routes.ABOUT) }
            )
        }
        composable(Routes.GAME) { entry ->
            GameScreen(
                viewModel = gameViewModel,
                settings = settings,
                onExit = { nav.backFrom(entry) },
                onGameOver = {
                    if (entry.isResumed) {
                        nav.navigate(Routes.RESULT) { popUpTo(Routes.GAME) { inclusive = true } }
                    }
                }
            )
        }
        composable(Routes.RESULT) { entry ->
            ResultScreen(
                summary = gameViewModel.summary,
                onPlayAgain = {
                    if (entry.isResumed) {
                        gameViewModel.newMatch()
                        nav.navigate(Routes.GAME) { popUpTo(Routes.RESULT) { inclusive = true } }
                    }
                },
                onHome = {
                    if (entry.isResumed) nav.popBackStack(Routes.HOME, inclusive = false)
                }
            )
        }
        composable(Routes.HOW_TO_PLAY) { entry -> HowToPlayScreen(onBack = { nav.backFrom(entry) }) }
        composable(Routes.SETTINGS) { entry ->
            SettingsScreen(settings, stats, appViewModel, onBack = { nav.backFrom(entry) })
        }
        composable(Routes.ABOUT) { entry -> AboutScreen(onBack = { nav.backFrom(entry) }) }
    }
}
