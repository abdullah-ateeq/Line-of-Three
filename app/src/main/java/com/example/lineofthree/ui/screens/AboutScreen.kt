package com.example.lineofthree.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.lineofthree.game.Player
import com.example.lineofthree.ui.board.BoardPreview
import com.example.lineofthree.ui.components.SectionLabel
import com.example.lineofthree.ui.components.SubScreen
import com.example.lineofthree.ui.theme.DisplayFamily

private val Features = listOf(
    "Local 2 Player",
    "Placement & Movement Phases",
    "Three-Piece Lines",
    "Piece Removal",
    "Offline Play"
)

@Composable
fun AboutScreen(onBack: () -> Unit) {
    SubScreen("About", onBack) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            BoardPreview(
                pieces = mapOf(1 to Player.PLAYER_ONE, 9 to Player.PLAYER_ONE, 13 to Player.PLAYER_TWO, 5 to Player.PLAYER_TWO),
                modifier = Modifier.size(140.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text("Line of Three", style = MaterialTheme.typography.displaySmall.copy(letterSpacing = MaterialTheme.typography.headlineSmall.letterSpacing), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(6.dp))
            Text("VERSION 1.0", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(20.dp))
            Text(
                "A refined local two-player strategy game where players create three-piece lines to remove opposing pieces and control the board.",
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = DisplayFamily, fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        SectionLabel("Features")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Features.forEach { feature ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val bronze = MaterialTheme.colorScheme.primary
                    Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.size(7.dp)) {
                            rotate(45f) { drawRect(bronze, Offset.Zero, Size(size.width, size.height)) }
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(feature, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
