package com.example.lineofthree.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.lineofthree.game.BoardRules
import com.example.lineofthree.game.Player
import com.example.lineofthree.ui.board.BoardGeometry
import com.example.lineofthree.ui.board.drawBoardPlate
import com.example.lineofthree.ui.board.drawConnections
import com.example.lineofthree.ui.board.drawLineAccent
import com.example.lineofthree.ui.board.drawSocket
import com.example.lineofthree.ui.board.drawToken
import com.example.lineofthree.ui.theme.LocalBoardPalette
import kotlin.math.PI
import kotlin.math.sin

private const val SPLASH_MS = 1400

/** Normalized progress of [t] within the window [start, end]. */
private fun window(t: Float, start: Float, end: Float) = ((t - start) / (end - start)).coerceIn(0f, 1f)

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }
    val finish by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(SPLASH_MS, easing = LinearEasing))
        finish()
    }
    val palette = LocalBoardPalette.current
    val topLine = BoardRules.validLines.indexOfFirst { it == listOf(0, 1, 2) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(Modifier.size(220.dp)) {
            val t = progress.value
            val geo = BoardGeometry.of(size)
            val boardAlpha = window(t, 0f, 0.28f)
            drawBoardPlate(geo, palette, boardAlpha)
            drawConnections(geo, palette, boardAlpha)
            BoardRules.nodeIds.forEach { drawSocket(geo.position(it), geo, palette, boardAlpha) }

            val appear = window(t, 0.18f, 0.36f)
            val r = geo.pieceRadius
            drawToken(geo.position(0), r * lerp(0.7f, 1f, appear), Player.PLAYER_ONE, alpha = appear)
            drawToken(geo.position(2), r * lerp(0.7f, 1f, appear), Player.PLAYER_ONE, alpha = appear)
            drawToken(geo.position(13), r * lerp(0.7f, 1f, appear), Player.PLAYER_TWO, alpha = appear)

            // Third piece slides into place to complete the top line.
            val slide = window(t, 0.36f, 0.54f)
            val moving = lerp(geo.position(9), geo.position(1), slide)
            drawToken(moving, r, Player.PLAYER_ONE, lift = sin(PI.toFloat() * slide) * 0.8f, alpha = appear)

            val line = window(t, 0.52f, 0.66f)
            if (line > 0f) drawLineAccent(geo, topLine, palette.accent.copy(alpha = 0.9f), geo.lineWidth * 1.6f, line)

            // Opponent piece pulses, shrinks and fades.
            val fade = window(t, 0.62f, 0.8f)
            val scale = if (fade < 0.3f) 1f + 0.12f * sin(PI.toFloat() * fade / 0.3f) else lerp(1f, 0.35f, (fade - 0.3f) / 0.7f)
            drawToken(geo.position(15), r * scale, Player.PLAYER_TWO, alpha = appear * (1f - fade))
        }
        Spacer(Modifier.height(36.dp))
        Text(
            "LINE OF THREE",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.graphicsLayer {
                val a = window(progress.value, 0.62f, 0.95f)
                alpha = a
                translationY = (1f - a) * 12.dp.toPx()
            }
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "A GAME OF LINES",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.graphicsLayer { alpha = window(progress.value, 0.75f, 1f) }
        )
    }
}
