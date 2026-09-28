package com.example.lineofthree.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.lineofthree.game.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode(val label: String) { LIGHT("Light"), DARK("Dark"), SYSTEM("System") }

enum class BoardTheme(val label: String) { CLASSIC("Classic"), MIDNIGHT("Midnight") }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val boardTheme: BoardTheme = BoardTheme.CLASSIC,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val showLegalMoves: Boolean = true,
    val highlightCompletedLines: Boolean = true
)

data class GameStatistics(
    val gamesPlayed: Int = 0,
    val player1Wins: Int = 0,
    val player2Wins: Int = 0,
    val mostLinesFormed: Int = 0
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "line_of_three")

class SettingsRepository(context: Context) {

    private val store = context.applicationContext.dataStore

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val BOARD = stringPreferencesKey("board_theme")
        val SOUND = booleanPreferencesKey("sound_enabled")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val LEGAL_MOVES = booleanPreferencesKey("show_legal_moves")
        val HIGHLIGHT_LINES = booleanPreferencesKey("highlight_lines")
        val GAMES_PLAYED = intPreferencesKey("games_played")
        val P1_WINS = intPreferencesKey("player1_wins")
        val P2_WINS = intPreferencesKey("player2_wins")
        val MOST_LINES = intPreferencesKey("most_lines")
    }

    val settings: Flow<AppSettings> = store.data.map { p ->
        AppSettings(
            themeMode = p[Keys.THEME].toEnum(ThemeMode.SYSTEM),
            boardTheme = p[Keys.BOARD].toEnum(BoardTheme.CLASSIC),
            soundEnabled = p[Keys.SOUND] ?: true,
            hapticsEnabled = p[Keys.HAPTICS] ?: true,
            showLegalMoves = p[Keys.LEGAL_MOVES] ?: true,
            highlightCompletedLines = p[Keys.HIGHLIGHT_LINES] ?: true
        )
    }

    val statistics: Flow<GameStatistics> = store.data.map { p ->
        GameStatistics(
            gamesPlayed = p[Keys.GAMES_PLAYED] ?: 0,
            player1Wins = p[Keys.P1_WINS] ?: 0,
            player2Wins = p[Keys.P2_WINS] ?: 0,
            mostLinesFormed = p[Keys.MOST_LINES] ?: 0
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = store.edit { it[Keys.THEME] = mode.name }
    suspend fun setBoardTheme(theme: BoardTheme) = store.edit { it[Keys.BOARD] = theme.name }
    suspend fun setSoundEnabled(enabled: Boolean) = store.edit { it[Keys.SOUND] = enabled }
    suspend fun setHapticsEnabled(enabled: Boolean) = store.edit { it[Keys.HAPTICS] = enabled }
    suspend fun setShowLegalMoves(enabled: Boolean) = store.edit { it[Keys.LEGAL_MOVES] = enabled }
    suspend fun setHighlightCompletedLines(enabled: Boolean) = store.edit { it[Keys.HIGHLIGHT_LINES] = enabled }

    suspend fun recordGame(winner: Player, linesFormedByWinner: Int) = store.edit { p ->
        p[Keys.GAMES_PLAYED] = (p[Keys.GAMES_PLAYED] ?: 0) + 1
        val winsKey = if (winner == Player.PLAYER_ONE) Keys.P1_WINS else Keys.P2_WINS
        p[winsKey] = (p[winsKey] ?: 0) + 1
        p[Keys.MOST_LINES] = maxOf(p[Keys.MOST_LINES] ?: 0, linesFormedByWinner)
    }

    suspend fun resetStatistics() = store.edit { p ->
        p.remove(Keys.GAMES_PLAYED)
        p.remove(Keys.P1_WINS)
        p.remove(Keys.P2_WINS)
        p.remove(Keys.MOST_LINES)
    }

    private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T =
        this?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default
}
