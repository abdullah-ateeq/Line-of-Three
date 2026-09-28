package com.example.lineofthree.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lineofthree.LineOfThreeApp
import com.example.lineofthree.data.AppSettings
import com.example.lineofthree.data.BoardTheme
import com.example.lineofthree.data.GameStatistics
import com.example.lineofthree.data.SettingsRepository
import com.example.lineofthree.data.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(private val repository: SettingsRepository) : ViewModel() {

    val settings: StateFlow<AppSettings?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val statistics: StateFlow<GameStatistics> = repository.statistics
        .stateIn(viewModelScope, SharingStarted.Eagerly, GameStatistics())

    fun setThemeMode(mode: ThemeMode) = update { it.setThemeMode(mode) }
    fun setBoardTheme(theme: BoardTheme) = update { it.setBoardTheme(theme) }
    fun setSoundEnabled(enabled: Boolean) = update { it.setSoundEnabled(enabled) }
    fun setHapticsEnabled(enabled: Boolean) = update { it.setHapticsEnabled(enabled) }
    fun setShowLegalMoves(enabled: Boolean) = update { it.setShowLegalMoves(enabled) }
    fun setHighlightCompletedLines(enabled: Boolean) = update { it.setHighlightCompletedLines(enabled) }
    fun resetStatistics() = update { it.resetStatistics() }

    private fun update(block: suspend (SettingsRepository) -> Unit) {
        viewModelScope.launch { block(repository) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LineOfThreeApp
                AppViewModel(app.settingsRepository)
            }
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LineOfThreeApp
                GameViewModel(app.settingsRepository)
            }
        }
    }
}
