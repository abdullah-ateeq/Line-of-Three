package com.example.lineofthree.game

/**
 * Holds the live match state plus undo history. Pure Kotlin so it can be unit tested.
 *
 * An undo snapshot is taken only immediately before a placement or movement, so a
 * move followed by its removal is undone as a single turn.
 */
class GameSession(initial: LineOfThreeState = GameEngine.newGame()) {

    var state: LineOfThreeState = initial
        private set

    private val history = ArrayDeque<LineOfThreeState>()

    val canUndo: Boolean
        get() = history.isNotEmpty() && !state.isAnimating && !state.isGameOver

    fun restart() {
        history.clear()
        state = GameEngine.newGame()
    }

    fun setAnimating(animating: Boolean) {
        state = state.copy(isAnimating = animating)
    }

    fun placePiece(nodeId: Int): ActionResult? =
        GameEngine.placePiece(state, nodeId)?.also { commit(it, snapshot = true) }

    fun movePiece(fromNodeId: Int, toNodeId: Int): ActionResult? =
        GameEngine.movePiece(state, fromNodeId, toNodeId)?.also { commit(it, snapshot = true) }

    fun removeOpponentPiece(nodeId: Int): ActionResult? =
        GameEngine.removeOpponentPiece(state, nodeId)?.also { commit(it, snapshot = false) }

    /**
     * Interprets a tap on [nodeId] according to the current phase. Returns the action
     * result when a turn-changing action occurred; selection changes return `null`.
     */
    fun tap(nodeId: Int): ActionResult? {
        val current = state
        if (current.isGameOver || current.isAnimating) return null
        return when (current.phase) {
            GamePhase.PLACEMENT -> placePiece(nodeId)
            GamePhase.REMOVE -> removeOpponentPiece(nodeId)
            GamePhase.MOVEMENT -> {
                val selected = current.selectedNodeId
                if (selected != null && nodeId in current.legalDestinationIds) {
                    movePiece(selected, nodeId)
                } else {
                    state = GameEngine.selectPiece(current, nodeId) ?: GameEngine.clearSelection(current)
                    null
                }
            }
        }
    }

    fun undo(): Boolean {
        if (!canUndo) return false
        state = history.removeLast()
        return true
    }

    private fun commit(result: ActionResult, snapshot: Boolean) {
        if (snapshot) {
            history.addLast(GameEngine.clearSelection(state).copy(newLineIds = emptySet()))
            if (history.size > MAX_HISTORY) history.removeFirst()
        }
        state = result.state
    }

    private companion object {
        const val MAX_HISTORY = 200
    }
}
