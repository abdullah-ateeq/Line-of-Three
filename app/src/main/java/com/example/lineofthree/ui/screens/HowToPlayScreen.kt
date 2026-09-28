package com.example.lineofthree.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.lineofthree.game.Player
import com.example.lineofthree.ui.board.BoardPreview
import com.example.lineofthree.ui.components.SectionLabel
import com.example.lineofthree.ui.components.SubScreen
import com.example.lineofthree.ui.components.TokenGlyph
import com.example.lineofthree.ui.theme.DisplayFamily

private val Rules = listOf(
    "Each player has 6 pieces.",
    "Take turns placing all your pieces onto empty board points.",
    "Form a line of three along one straight board segment to remove one opponent piece.",
    "Once every piece is placed, move one piece per turn to an adjacent connected point. No jumping.",
    "A line that already exists does not grant removals again — break it and re-form it to strike again.",
    "Pieces outside completed lines must be removed first. Pieces in lines are protected unless none remain outside.",
    "Win by reducing your opponent below 3 pieces, or by leaving them with no legal move."
)

private val DiagramPieces = mapOf(
    0 to Player.PLAYER_ONE, 1 to Player.PLAYER_ONE, 2 to Player.PLAYER_ONE,
    8 to Player.PLAYER_TWO, 9 to Player.PLAYER_TWO, 10 to Player.PLAYER_TWO, 13 to Player.PLAYER_TWO
)

@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    SubScreen("How to Play", onBack) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            BoardPreview(
                pieces = DiagramPieces,
                highlightLines = setOf(0),
                markedNodes = setOf(13),
                modifier = Modifier.size(220.dp),
                description = "Diagram: Player 1 has completed the top line. Player 2's top inner line is protected, so only their lower piece may be removed."
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Player 1 completes a line. Player 2's inner line is protected, so only the marked piece can be taken.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        SectionLabel("Players")
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Legend(Player.PLAYER_ONE, "Copper · Diamond")
            Legend(Player.PLAYER_TWO, "Teal · Ring")
        }

        SectionLabel("Rules")
        Column(Modifier.widthIn(max = 560.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Rules.forEachIndexed { i, rule ->
                Row {
                    Text(
                        "${i + 1}",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = DisplayFamily),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(28.dp)
                    )
                    Text(rule, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun Legend(player: Player, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TokenGlyph(player, 28.dp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(player.label.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
