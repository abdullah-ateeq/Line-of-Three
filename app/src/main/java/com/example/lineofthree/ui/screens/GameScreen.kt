package com.example.lineofthree.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lineofthree.data.AppSettings
import com.example.lineofthree.feedback.Haptic
import com.example.lineofthree.feedback.Haptics
import com.example.lineofthree.feedback.LocalSoundEffects
import com.example.lineofthree.feedback.Sound
import com.example.lineofthree.game.GameEvent
import com.example.lineofthree.game.GamePhase
import com.example.lineofthree.game.LineOfThreeState
import com.example.lineofthree.game.Player
import com.example.lineofthree.ui.GameUiState
import com.example.lineofthree.ui.GameViewModel
import com.example.lineofthree.ui.board.GameBoard
import com.example.lineofthree.ui.components.ConfirmDialog
import com.example.lineofthree.ui.components.TokenGlyph
import com.example.lineofthree.ui.theme.tokens
import kotlinx.coroutines.delay

private val RemoveTint = Color(0xFFD9786B)

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    settings: AppSettings,
    onExit: () -> Unit,
    onGameOver: () -> Unit
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val state = ui.game
    var showRestart by rememberSaveable { mutableStateOf(false) }
    var showLeave by rememberSaveable { mutableStateOf(false) }

    GameFeedback(viewModel, settings)

    val gameOver by rememberUpdatedState(onGameOver)
    LaunchedEffect(state.winner) {
        if (state.winner != null) {
            delay(1100)
            gameOver()
        }
    }

    val requestExit = { if (state.turns > 0 && !state.isGameOver) showLeave = true else onExit() }
    BackHandler(enabled = !state.isGameOver) { requestExit() }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        val landscape = maxWidth > maxHeight
        val board: @Composable (Modifier) -> Unit = { mod ->
            GameBoard(
                ui = ui,
                showLegalMoves = settings.showLegalMoves,
                highlightCompletedLines = settings.highlightCompletedLines,
                onNodeTap = viewModel::onNodeTapped,
                modifier = mod
            )
        }
        val controls: @Composable () -> Unit = {
            GameControls(
                ui = ui,
                onUndo = viewModel::undo,
                onRestart = { showRestart = true },
                onMenu = requestExit
            )
        }

        if (landscape) {
            Row(
                Modifier.fillMaxSize().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BoxWithConstraints(Modifier.fillMaxHeight().weight(1f), contentAlignment = Alignment.Center) {
                    board(Modifier.size(minOf(maxWidth, maxHeight)))
                }
                Column(
                    Modifier.widthIn(min = 260.dp, max = 380.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                ) {
                    PlayerPanel(Player.PLAYER_TWO, state)
                    PhaseStatus(state)
                    PlayerPanel(Player.PLAYER_ONE, state)
                    Spacer(Modifier.height(4.dp))
                    controls()
                }
            }
        } else {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlayerPanel(Player.PLAYER_TWO, state, Modifier.widthIn(max = 560.dp))
                PhaseStatus(state, Modifier.widthIn(max = 560.dp))
                BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    board(Modifier.size(minOf(maxWidth, maxHeight, 560.dp)))
                }
                PlayerPanel(Player.PLAYER_ONE, state, Modifier.widthIn(max = 560.dp))
                Box(Modifier.widthIn(max = 560.dp)) { controls() }
            }
        }
    }

    if (showRestart) {
        ConfirmDialog(
            title = "Restart this match?",
            message = null,
            confirmLabel = "Restart",
            onConfirm = { showRestart = false; viewModel.newMatch() },
            onDismiss = { showRestart = false }
        )
    }
    if (showLeave) {
        ConfirmDialog(
            title = "Leave this match?",
            message = "The current match will not be saved.",
            confirmLabel = "Leave",
            onConfirm = { showLeave = false; onExit() },
            onDismiss = { showLeave = false }
        )
    }
}

/** Plays sounds and haptics for game events, respecting the user's settings. */
@Composable
private fun GameFeedback(viewModel: GameViewModel, settings: AppSettings) {
    val view = LocalView.current
    val sounds = LocalSoundEffects.current
    val current by rememberUpdatedState(settings)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val (sound, haptic) = when (event) {
                is GameEvent.Placed -> Sound.PLACE to Haptic.PLACE
                is GameEvent.Moved -> Sound.MOVE to Haptic.MOVE
                is GameEvent.LineFormed -> Sound.LINE to Haptic.LINE
                is GameEvent.Removed -> Sound.REMOVE to Haptic.REMOVE
                is GameEvent.Won -> Sound.WIN to Haptic.WIN
            }
            if (current.soundEnabled) sounds?.play(sound)
            if (current.hapticsEnabled) Haptics.perform(view, haptic)
        }
    }
}

@Composable
private fun PlayerPanel(player: Player, state: LineOfThreeState, modifier: Modifier = Modifier) {
    val active = state.currentPlayer == player && !state.isGameOver
    val isWinner = state.winner == player
    val tokens = player.tokens()
    val borderColor by animateColorAsState(
        if (active || isWinner) tokens.light.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outlineVariant,
        tween(250), label = "panelBorder"
    )
    val contentAlpha by animateFloatAsState(if (active || isWinner || state.isGameOver) 1f else 0.62f, tween(250), label = "panelAlpha")
    val shape = RoundedCornerShape(8.dp)
    val onBoard = state.piecesOnBoard(player)
    val toPlace = state.piecesToPlace(player)

    Row(
        modifier
            .fillMaxWidth()
            .background(if (active) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow, shape)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .alpha(contentAlpha)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            TokenGlyph(player, 38.dp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(player.label.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            val status = when {
                isWinner -> "Victory"
                active && state.phase == GamePhase.REMOVE -> "Removing"
                active -> "Your turn"
                else -> "Waiting"
            }
            Text(
                status.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (active || isWinner) tokens.light else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (toPlace > 0) {
            PanelStat(onBoard.toString(), "On Board")
            Spacer(Modifier.width(16.dp))
            PanelStat(toPlace.toString(), "To Place")
        } else {
            PanelStat(state.piecesRemaining(player).toString(), "Pieces Remaining")
        }
    }
}

@Composable
private fun PanelStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PhaseStatus(state: LineOfThreeState, modifier: Modifier = Modifier) {
    val phaseLabel = when (state.phase) {
        GamePhase.PLACEMENT -> "PLACEMENT"
        GamePhase.MOVEMENT -> "MOVEMENT"
        GamePhase.REMOVE -> "REMOVE"
    }
    val instruction = instructionFor(state)
    val removing = state.phase == GamePhase.REMOVE && !state.isGameOver
    val chipColor by animateColorAsState(
        if (removing) RemoveTint else MaterialTheme.colorScheme.primary, tween(250), label = "chip"
    )

    Column(
        modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).background(chipColor, RoundedCornerShape(1.dp)))
            Spacer(Modifier.width(8.dp))
            Text(
                if (state.isGameOver) "MATCH OVER" else phaseLabel,
                style = MaterialTheme.typography.labelMedium,
                color = chipColor
            )
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(6.dp).background(chipColor, RoundedCornerShape(1.dp)))
        }
        Spacer(Modifier.height(4.dp))
        AnimatedContent(
            targetState = instruction,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "instruction"
        ) { text ->
            Text(
                text,
                style = if (removing) MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                else MaterialTheme.typography.bodyMedium,
                color = if (removing) RemoveTint else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun instructionFor(state: LineOfThreeState): String {
    val p = state.currentPlayer.label
    state.winner?.let { return "${it.label} wins" }
    return when (state.phase) {
        GamePhase.PLACEMENT -> "$p — place a piece on any open point"
        GamePhase.MOVEMENT ->
            if (state.selectedNodeId != null) {
                if (state.legalDestinationIds.isEmpty()) "$p — this piece cannot move" else "$p — choose a connected open point"
            } else "$p — select a piece to move"
        GamePhase.REMOVE -> {
            val opponentPieces = state.piecesOnBoard(state.currentPlayer.opponent)
            if (state.removableNodeIds.size < opponentPieces) "REMOVE ONE OPPONENT PIECE · LINES PROTECTED"
            else "REMOVE ONE OPPONENT PIECE"
        }
    }
}

@Composable
private fun GameControls(ui: GameUiState, onUndo: () -> Unit, onRestart: () -> Unit, onMenu: () -> Unit) {
    val state = ui.game
    val dimmed = state.phase == GamePhase.REMOVE || state.isGameOver
    val controlsAlpha by animateFloatAsState(if (dimmed) 0.5f else 1f, tween(200), label = "controls")
    Row(
        Modifier.fillMaxWidth().alpha(controlsAlpha),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ControlButton("Undo", ui.canUndo && !state.isAnimating, onUndo, Modifier.weight(1f))
        ControlButton("Restart", !state.isGameOver, onRestart, Modifier.weight(1f))
        ControlButton("Menu", !state.isGameOver, onMenu, Modifier.weight(1f))
    }
}

@Composable
private fun ControlButton(label: String, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (enabled) 1f else 0.4f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        contentPadding = ButtonDefaults.TextButtonContentPadding
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium)
    }
}
