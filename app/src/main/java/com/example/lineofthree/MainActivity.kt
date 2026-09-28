package com.example.lineofthree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lineofthree.data.AppSettings
import com.example.lineofthree.feedback.LocalSoundEffects
import com.example.lineofthree.feedback.SoundEffects
import com.example.lineofthree.ui.AppNavigation
import com.example.lineofthree.ui.AppViewModel
import com.example.lineofthree.ui.theme.LineOfThreeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)
            val loaded by appViewModel.settings.collectAsStateWithLifecycle()
            val settings = loaded ?: AppSettings()
            val sounds = remember { SoundEffects(applicationContext) }
            DisposableEffect(sounds) { onDispose { sounds.release() } }

            LineOfThreeTheme(themeMode = settings.themeMode, boardTheme = settings.boardTheme) {
                CompositionLocalProvider(LocalSoundEffects provides sounds) {
                    AppNavigation(appViewModel, settings)
                }
            }
        }
    }
}
