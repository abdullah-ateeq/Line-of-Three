package com.example.lineofthree.ui

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lineofthree.data.SettingsRepository
import com.example.lineofthree.game.ActionResult
import com.example.lineofthree.game.GameEvent
import com.example.lineofthree.game.GameSession
import com.example.lineofthree.game.LineOfThreeState
import com.example.lineofthree.game.PiecePlacement
import com.example.lineofthree.game.Player
import com.example.lineofthree.game.WinReason
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** A piece sliding between two nodes; [id] restarts the animation for each new move. */
data class PieceMotion(val fromNodeId: Int, val toNodeId: Int, val id: Long)

data class GameUiState(
    val game: LineOfThreeState = LineOfThreeState(),
    val canUndo: Boolean = false,
    val vanishing: PiecePlacement? = null,
    val motion: PieceMotion? = null,
    val placedNodeId: Int? = null,
    val actionId: Long = 0L
)

data class MatchSummary(
    val winner: Player,
    val reason: WinReason,
    val linesFormed: Map<Player, Int>,
    val piecesRemoved: Map<Player, Int>,
    val piecesRemaining: Map<Player, Int>,
    val turns: Int,
    val durationMillis: Long
)

class GameViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val session = GameSession()
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private var startedAt = SystemClock.elapsedRealtime()
    private var finishedAt: Long? = null
    private var animationJob: Job? = null
    private var actionCounter = 0L

    val summary: MatchSummary?
        get() {
            val s = session.state
            val winner = s.winner ?: return null
            val players = Player.entries
            return MatchSummary(
                winner = winner,
                reason = s.winReason ?: WinReason.REDUCED,
                linesFormed = players.associateWith { s.linesFormed(it) },
                piecesRemoved = players.associateWith { s.piecesRemovedBy(it) },
                piecesRemaining = players.associateWith { s.piecesRemaining(it) },
                turns = s.turns,
                durationMillis = (finishedAt ?: SystemClock.elapsedRealtime()) - startedAt
            )
        }

    fun newMatch() {
        animationJob?.cancel()
        session.restart()
        startedAt = SystemClock.elapsedRealtime()
        finishedAt = null
        _uiState.value = GameUiState(game = session.state)
    }

    fun onNodeTapped(nodeId: Int) {
        val result = session.tap(nodeId)
        if (result == null) {
            publish()
            return
        }
        handle(result)
    }

    fun placePiece(nodeId: Int) {
        session.placePiece(nodeId)?.let(::handle)
    }

    fun movePiece(fromNodeId: Int, toNodeId: Int) {
        session.movePiece(fromNodeId, toNodeId)?.let(::handle)
    }

    fun removeOpponentPiece(nodeId: Int) {
        session.removeOpponentPiece(nodeId)?.let(::handle)
    }

    fun undo() {
        if (session.undo()) {
            _uiState.value = GameUiState(game = session.state, canUndo = session.canUndo, actionId = ++actionCounter)
        }
    }

    private fun handle(result: ActionResult) {
        actionCounter++
        var motion: PieceMotion? = null
        var placed: Int? = null
        var vanishing: PiecePlacement? = null

        result.events.forEach { event ->
            when (event) {
                is GameEvent.Placed -> placed = event.nodeId
                is GameEvent.Moved -> motion = PieceMotion(event.fromNodeId, event.toNodeId, actionCounter)
                is GameEvent.Removed -> vanishing = PiecePlacement(event.nodeId, event.owner)
                is GameEvent.Won -> onMatchWon(event.winner)
                is GameEvent.LineFormed -> Unit
            }
            _events.tryEmit(event)
        }

        if (vanishing != null) {
            session.setAnimating(true)
            animationJob?.cancel()
            animationJob = viewModelScope.launch {
                delay(REMOVE_ANIMATION_MS)
                session.setAnimating(false)
                _uiState.value = _uiState.value.copy(
                    game = session.state,
                    canUndo = session.canUndo,
                    vanishing = null
                )
            }
        }
        _uiState.value = GameUiState(
            game = session.state,
            canUndo = session.canUndo,
            vanishing = vanishing,
            motion = motion,
            placedNodeId = placed,
            actionId = actionCounter
        )
    }

    private fun onMatchWon(winner: Player) {
        finishedAt = SystemClock.elapsedRealtime()
        val lines = session.state.linesFormed(winner)
        viewModelScope.launch { repository.recordGame(winner, lines) }
    }

    private fun publish() {
        _uiState.value = _uiState.value.copy(game = session.state, canUndo = session.canUndo)
    }

    companion object {
        const val REMOVE_ANIMATION_MS = 220L
    }
}
