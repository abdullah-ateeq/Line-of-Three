package com.example.lineofthree.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.lineofthree.data.GameStatistics
import com.example.lineofthree.game.Player
import com.example.lineofthree.ui.board.BoardPreview
import com.example.lineofthree.ui.components.PrimaryButton
import com.example.lineofthree.ui.components.SecondaryButton
import com.example.lineofthree.ui.components.StatBlock
import com.example.lineofthree.ui.theme.DisplayFamily

private val EmblemPieces = mapOf(
    0 to Player.PLAYER_ONE, 1 to Player.PLAYER_ONE, 2 to Player.PLAYER_ONE,
    9 to Player.PLAYER_TWO, 13 to Player.PLAYER_TWO, 11 to Player.PLAYER_TWO
)

@Composable
fun HomeScreen(
    stats: GameStatistics,
    onPlay: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit
) {
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        val landscape = maxWidth > maxHeight && maxHeight < 560.dp
        val emblemLandscape = minOf(maxHeight * 0.42f, 180.dp)
        val emblemPortrait = minOf(maxWidth * 0.5f, 220.dp)
        if (landscape) {
            Row(
                Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally)
            ) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Emblem(Modifier.size(emblemLandscape))
                    Spacer(Modifier.height(20.dp))
                    Title()
                }
                Column(
                    Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Actions(onPlay, onHowToPlay, onSettings, onAbout)
                    Spacer(Modifier.height(20.dp))
                    StatsRow(stats)
                }
            }
        } else {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.height(32.dp))
                Emblem(Modifier.size(emblemPortrait))
                Spacer(Modifier.height(32.dp))
                Title()
                Spacer(Modifier.height(40.dp))
                Actions(onPlay, onHowToPlay, onSettings, onAbout)
                Spacer(Modifier.height(32.dp))
                StatsRow(stats)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun Emblem(modifier: Modifier) {
    BoardPreview(pieces = EmblemPieces, highlightLines = setOf(0), modifier = modifier)
}

@Composable
private fun Title() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "LINE OF THREE",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Form the line. Break their defense.",
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = DisplayFamily, fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Actions(onPlay: () -> Unit, onHowToPlay: () -> Unit, onSettings: () -> Unit, onAbout: () -> Unit) {
    Column(
        Modifier.widthIn(max = 340.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PrimaryButton("Play", onPlay, Modifier.fillMaxWidth())
        Spacer(Modifier.height(4.dp))
        SecondaryButton("How to Play", onHowToPlay, Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("Settings", onSettings, Modifier.weight(1f))
            SecondaryButton("About", onAbout, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatsRow(stats: GameStatistics) {
    Row(
        Modifier.widthIn(max = 340.dp).fillMaxWidth().height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatBlock(stats.gamesPlayed.toString(), "Games", Modifier.weight(1f))
        VerticalDivider(Modifier.height(28.dp), color = MaterialTheme.colorScheme.outlineVariant)
        StatBlock(stats.player1Wins.toString(), "P1 Wins", Modifier.weight(1f))
        VerticalDivider(Modifier.height(28.dp), color = MaterialTheme.colorScheme.outlineVariant)
        StatBlock(stats.player2Wins.toString(), "P2 Wins", Modifier.weight(1f))
    }
}
