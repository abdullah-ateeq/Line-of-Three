package com.example.lineofthree.game

/**
 * Pure rule engine. Every action returns a new [ActionResult], or `null` when the
 * action is illegal in the given state. No function here mutates its input.
 */
object GameEngine {

    const val PIECES_PER_PLAYER = 6
    const val MIN_PIECES_TO_COMPETE = 3

    fun newGame(): LineOfThreeState = LineOfThreeState()

    // ---- Queries ---------------------------------------------------------------------

    fun getCompletedLineIds(pieces: Map<Int, Player>, player: Player): Set<Int> =
        BoardRules.validLines.indices
            .filter { id -> BoardRules.validLines[id].all { pieces[it] == player } }
            .toSet()

    fun getCompletedLineIds(state: LineOfThreeState, player: Player): Set<Int> =
        getCompletedLineIds(state.pieces, player)

    /** Nodes that belong to at least one completed line of [player]. */
    fun nodesInCompletedLines(pieces: Map<Int, Player>, player: Player): Set<Int> =
        getCompletedLineIds(pieces, player).flatMap { BoardRules.validLines[it] }.toSet()

    /**
     * Opponent pieces outside completed lines are removable. Only when every opponent
     * piece sits inside a completed line may any of them be removed.
     */
    fun getRemovableOpponentNodes(pieces: Map<Int, Player>, opponent: Player): Set<Int> {
        val opponentNodes = pieces.filterValues { it == opponent }.keys
        val outsideLines = opponentNodes - nodesInCompletedLines(pieces, opponent)
        return outsideLines.ifEmpty { opponentNodes }
    }

    fun getRemovableOpponentNodes(state: LineOfThreeState, opponent: Player): Set<Int> =
        getRemovableOpponentNodes(state.pieces, opponent)

    fun legalDestinations(pieces: Map<Int, Player>, fromNodeId: Int): Set<Int> =
        BoardRules.neighbors(fromNodeId).filterTo(mutableSetOf()) { it !in pieces }

    fun hasLegalMove(pieces: Map<Int, Player>, player: Player): Boolean =
        pieces.any { (node, owner) -> owner == player && legalDestinations(pieces, node).isNotEmpty() }

    private fun LineOfThreeState.canAct(): Boolean = winner == null && !isAnimating

    // ---- Actions ---------------------------------------------------------------------

    fun placePiece(state: LineOfThreeState, nodeId: Int): ActionResult? {
        if (!state.canAct() || state.phase != GamePhase.PLACEMENT) return null
        if (nodeId !in BoardRules.nodeIds || nodeId in state.pieces) return null
        val player = state.currentPlayer
        val toPlace = state.piecesToPlace(player)
        if (toPlace <= 0) return null

        val linesBefore = getCompletedLineIds(state.pieces, player)
        val placed = state.copy(
            pieces = state.pieces + (nodeId to player),
            player1PiecesToPlace = if (player == Player.PLAYER_ONE) toPlace - 1 else state.player1PiecesToPlace,
            player2PiecesToPlace = if (player == Player.PLAYER_TWO) toPlace - 1 else state.player2PiecesToPlace,
            selectedNodeId = null,
            legalDestinationIds = emptySet(),
            newLineIds = emptySet()
        )
        return resolveAfterAction(placed, linesBefore, GameEvent.Placed(nodeId, player))
    }

    /** Selects (or toggles off) one of the current player's pieces during movement. */
    fun selectPiece(state: LineOfThreeState, nodeId: Int): LineOfThreeState? {
        if (!state.canAct() || state.phase != GamePhase.MOVEMENT) return null
        if (state.pieces[nodeId] != state.currentPlayer) return null
        if (state.selectedNodeId == nodeId) return clearSelection(state)
        return state.copy(
            selectedNodeId = nodeId,
            legalDestinationIds = legalDestinations(state.pieces, nodeId)
        )
    }

    fun clearSelection(state: LineOfThreeState): LineOfThreeState =
        state.copy(selectedNodeId = null, legalDestinationIds = emptySet())

    fun movePiece(state: LineOfThreeState, fromNodeId: Int, toNodeId: Int): ActionResult? {
        if (!state.canAct() || state.phase != GamePhase.MOVEMENT) return null
        val player = state.currentPlayer
        if (state.pieces[fromNodeId] != player) return null
        if (toNodeId !in BoardRules.nodeIds || toNodeId in state.pieces) return null
        if (!BoardRules.isAdjacent(fromNodeId, toNodeId)) return null

        val linesBefore = getCompletedLineIds(state.pieces, player)
        val moved = state.copy(
            pieces = state.pieces - fromNodeId + (toNodeId to player),
            selectedNodeId = null,
            legalDestinationIds = emptySet(),
            newLineIds = emptySet()
        )
        return resolveAfterAction(moved, linesBefore, GameEvent.Moved(fromNodeId, toNodeId, player))
    }

    fun removeOpponentPiece(state: LineOfThreeState, nodeId: Int): ActionResult? {
        if (!state.canAct() || state.phase != GamePhase.REMOVE) return null
        val player = state.currentPlayer
        val opponent = player.opponent
        if (state.pieces[nodeId] != opponent) return null
        if (nodeId !in getRemovableOpponentNodes(state.pieces, opponent)) return null

        val removed = state.copy(
            pieces = state.pieces - nodeId,
            player1Removed = state.player1Removed + if (player == Player.PLAYER_ONE) 1 else 0,
            player2Removed = state.player2Removed + if (player == Player.PLAYER_TWO) 1 else 0,
            removableNodeIds = emptySet()
        )
        val removedEvent = GameEvent.Removed(nodeId, opponent)

        if (removed.piecesRemaining(opponent) < MIN_PIECES_TO_COMPETE) {
            val won = removed.copy(
                winner = player,
                winReason = WinReason.REDUCED,
                turns = removed.turns + 1,
                selectedNodeId = null,
                legalDestinationIds = emptySet()
            )
            return ActionResult(won, listOf(removedEvent, GameEvent.Won(player, WinReason.REDUCED)))
        }
        val finished = finishTurn(removed)
        return ActionResult(finished.state, listOf(removedEvent) + finished.events)
    }

    /**
     * Ends the current turn: clears transient selection state, hands the turn to the
     * opponent and, once movement has begun, checks whether that opponent is blocked.
     */
    fun finishTurn(state: LineOfThreeState): ActionResult {
        val previous = state.currentPlayer
        val next = previous.opponent
        val phase = if (state.allPiecesPlaced) GamePhase.MOVEMENT else GamePhase.PLACEMENT
        val handedOver = state.copy(
            currentPlayer = next,
            phase = phase,
            selectedNodeId = null,
            legalDestinationIds = emptySet(),
            removableNodeIds = emptySet(),
            turns = state.turns + 1
        )
        if (phase == GamePhase.MOVEMENT && !hasLegalMove(handedOver.pieces, next)) {
            val won = handedOver.copy(winner = previous, winReason = WinReason.BLOCKED)
            return ActionResult(won, listOf(GameEvent.Won(previous, WinReason.BLOCKED)))
        }
        return ActionResult(handedOver, emptyList())
    }

    /**
     * Compares completed lines before and after an action. Any newly completed line
     * grants exactly one removal, no matter how many lines appeared at once.
     */
    private fun resolveAfterAction(
        state: LineOfThreeState,
        linesBefore: Set<Int>,
        actionEvent: GameEvent
    ): ActionResult {
        val player = state.currentPlayer
        val newLines = getCompletedLineIds(state.pieces, player) - linesBefore
        val removable = getRemovableOpponentNodes(state.pieces, player.opponent)

        if (newLines.isNotEmpty() && removable.isNotEmpty()) {
            val removeMode = state.copy(
                phase = GamePhase.REMOVE,
                removableNodeIds = removable,
                newLineIds = newLines,
                player1LinesFormed = state.player1LinesFormed + if (player == Player.PLAYER_ONE) newLines.size else 0,
                player2LinesFormed = state.player2LinesFormed + if (player == Player.PLAYER_TWO) newLines.size else 0
            )
            return ActionResult(removeMode, listOf(actionEvent, GameEvent.LineFormed(newLines, player)))
        }
        val finished = finishTurn(state)
        return ActionResult(finished.state, listOf(actionEvent) + finished.events)
    }
}
