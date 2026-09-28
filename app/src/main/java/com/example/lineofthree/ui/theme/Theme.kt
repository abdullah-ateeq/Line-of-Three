package com.example.lineofthree.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.lineofthree.data.BoardTheme
import com.example.lineofthree.data.ThemeMode
import com.example.lineofthree.game.Player

private val DarkColors = darkColorScheme(
    primary = Bronze,
    onPrimary = Color(0xFF1A140C),
    secondary = Color(0xFFB46A4A),
    tertiary = Color(0xFF3F8F8A),
    background = Graphite,
    onBackground = Ivory,
    surface = GraphiteSurface,
    onSurface = Ivory,
    surfaceVariant = GraphiteRaised,
    onSurfaceVariant = IvoryMuted,
    surfaceContainerLowest = Graphite,
    surfaceContainerLow = GraphiteSurface,
    surfaceContainer = GraphiteRaised,
    surfaceContainerHigh = GraphiteHigh,
    surfaceContainerHighest = GraphiteHigh,
    outline = OutlineDark,
    outlineVariant = OutlineDarkSoft,
    error = Brick
)

private val LightColors = lightColorScheme(
    primary = BronzeDeep,
    onPrimary = Color(0xFFFFF8EE),
    secondary = Color(0xFF9A5238),
    tertiary = Color(0xFF1F6B6E),
    background = Stone,
    onBackground = Ink,
    surface = StoneSurface,
    onSurface = Ink,
    surfaceVariant = StoneRaised,
    onSurfaceVariant = InkMuted,
    surfaceContainerLowest = StoneSurface,
    surfaceContainerLow = StoneSurface,
    surfaceContainer = StoneRaised,
    surfaceContainerHigh = StoneHigh,
    surfaceContainerHighest = StoneHigh,
    outline = OutlineLight,
    outlineVariant = OutlineLightSoft,
    error = Color(0xFF9E3F37)
)

/** Colors for one player's tokens. */
data class TokenColors(
    val base: Color,
    val light: Color,
    val dark: Color,
    val symbol: Color
)

val Player1Tokens = TokenColors(
    base = Color(0xFFA65A3C),
    light = Color(0xFFD69474),
    dark = Color(0xFF5E2C1B),
    symbol = Color(0xFFF3D9C6)
)

val Player2Tokens = TokenColors(
    base = Color(0xFF1E6A6E),
    light = Color(0xFF58A3A2),
    dark = Color(0xFF0C3538),
    symbol = Color(0xFFC6E6E2)
)

fun Player.tokens(): TokenColors = if (this == Player.PLAYER_ONE) Player1Tokens else Player2Tokens

/** Colors used to paint the board plate, its engraved connections and sockets. */
data class BoardPalette(
    val plateTop: Color,
    val plateBottom: Color,
    val plateEdge: Color,
    val line: Color,
    val lineHighlight: Color,
    val socket: Color,
    val socketRim: Color,
    val accent: Color,
    val emblem: Color
)

val ClassicBoard = BoardPalette(
    plateTop = Color(0xFF3C2B20),
    plateBottom = Color(0xFF281B13),
    plateEdge = Color(0xFF5A4431),
    line = Color(0xFFB08A55),
    lineHighlight = Color(0x33FFE7C2),
    socket = Color(0xFF1A110B),
    socketRim = Color(0xFF6E5439),
    accent = Color(0xFFE6C27A),
    emblem = Color(0x40B08A55)
)

val MidnightBoard = BoardPalette(
    plateTop = Color(0xFF212833),
    plateBottom = Color(0xFF151A22),
    plateEdge = Color(0xFF33404F),
    line = Color(0xFF9AA6B4),
    lineHighlight = Color(0x26E4ECF5),
    socket = Color(0xFF0A0D12),
    socketRim = Color(0xFF3D4A5A),
    accent = Color(0xFFDCC48E),
    emblem = Color(0x409AA6B4)
)

fun BoardTheme.palette(): BoardPalette = when (this) {
    BoardTheme.CLASSIC -> ClassicBoard
    BoardTheme.MIDNIGHT -> MidnightBoard
}

val LocalBoardPalette = staticCompositionLocalOf { ClassicBoard }

@Composable
fun LineOfThreeTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    boardTheme: BoardTheme = BoardTheme.CLASSIC,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    CompositionLocalProvider(LocalBoardPalette provides boardTheme.palette()) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = Typography,
            content = content
        )
    }
}
