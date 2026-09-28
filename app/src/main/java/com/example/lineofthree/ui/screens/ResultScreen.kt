package com.example.lineofthree.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.lineofthree.game.Player
import com.example.lineofthree.game.WinReason
import com.example.lineofthree.ui.MatchSummary
import com.example.lineofthree.ui.components.HairlineDivider
import com.example.lineofthree.ui.components.PrimaryButton
import com.example.lineofthree.ui.components.SecondaryButton
import com.example.lineofthree.ui.components.StatBlock
import com.example.lineofthree.ui.components.TokenGlyph
import com.example.lineofthree.ui.theme.tokens

@Composable
fun ResultScreen(
    summary: MatchSummary?,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    val home by rememberUpdatedState(onHome)
    if (summary == null) {
        LaunchedEffect(Unit) { home() }
        return
    }
    BackHandler { onHome() }

    val winnerColor = summary.winner.tokens().light
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TokenGlyph(summary.winner, 88.dp)
        Spacer(Modifier.height(20.dp))
        Text(
            "${summary.winner.label.uppercase()} WINS",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(10.dp))
        Text(
            when (summary.reason) {
                WinReason.REDUCED -> "OPPONENT REDUCED BELOW 3"
                WinReason.BLOCKED -> "OPPONENT BLOCKED"
            },
            style = MaterialTheme.typography.labelLarge,
            color = winnerColor
        )
        Spacer(Modifier.height(32.dp))

        val shape = RoundedCornerShape(10.dp)
        Column(
            Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow, shape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                .padding(vertical = 8.dp)
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1.4f))
                Player.entries.forEach { p ->
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        TokenGlyph(p, 20.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("P${p.number}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            HairlineDivider(Modifier.padding(horizontal = 16.dp))
            ComparisonRow("Lines Formed", summary.linesFormed)
            ComparisonRow("Pieces Removed", summary.piecesRemoved)
            ComparisonRow("Pieces Remaining", summary.piecesRemaining)
            HairlineDivider(Modifier.padding(horizontal = 16.dp))
            Row(Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
                StatBlock(summary.turns.toString(), "Turns Played", Modifier.weight(1f))
                StatBlock(formatDuration(summary.durationMillis), "Match Duration", Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(32.dp))
        Column(Modifier.widthIn(max = 340.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PrimaryButton("Play Again", onPlayAgain, Modifier.fillMaxWidth())
            SecondaryButton("Home", onHome, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ComparisonRow(label: String, values: Map<Player, Int>) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.4f))
        Player.entries.forEach { p ->
            Text(
                values[p]?.toString() ?: "0",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
