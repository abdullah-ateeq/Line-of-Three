package com.example.lineofthree.game

enum class Player(val number: Int) {
    PLAYER_ONE(1),
    PLAYER_TWO(2);

    val opponent: Player
        get() = if (this == PLAYER_ONE) PLAYER_TWO else PLAYER_ONE

    val label: String
        get() = "Player $number"
}

enum class GamePhase {
    PLACEMENT,
    MOVEMENT,
    REMOVE
}

enum class WinReason(val label: String) {
    REDUCED("Opponent reduced below 3"),
    BLOCKED("Opponent blocked")
}

data class PiecePlacement(
    val nodeId: Int,
    val player: Player
)

data class LineOfThreeState(
    val pieces: Map<Int, Player> = emptyMap(),
    val currentPlayer: Player = Player.PLAYER_ONE,
    val phase: GamePhase = GamePhase.PLACEMENT,
    val player1PiecesToPlace: Int = GameEngine.PIECES_PER_PLAYER,
    val player2PiecesToPlace: Int = GameEngine.PIECES_PER_PLAYER,
    val selectedNodeId: Int? = null,
    val legalDestinationIds: Set<Int> = emptySet(),
    val removableNodeIds: Set<Int> = emptySet(),
    /** Line IDs formed by the most recent action; drives the line-formed highlight. */
    val newLineIds: Set<Int> = emptySet(),
    val winner: Player? = null,
    val winReason: WinReason? = null,
    val turns: Int = 0,
    val player1LinesFormed: Int = 0,
    val player2LinesFormed: Int = 0,
    /** Opponent pieces removed BY each player. */
    val player1Removed: Int = 0,
    val player2Removed: Int = 0,
    val isAnimating: Boolean = false
) {
    val isGameOver: Boolean get() = winner != null

    fun piecesToPlace(player: Player): Int =
        if (player == Player.PLAYER_ONE) player1PiecesToPlace else player2PiecesToPlace

    fun piecesOnBoard(player: Player): Int = pieces.count { it.value == player }

    /** Pieces a player still has in play: on the board plus any not yet placed. */
    fun piecesRemaining(player: Player): Int = piecesOnBoard(player) + piecesToPlace(player)

    fun linesFormed(player: Player): Int =
        if (player == Player.PLAYER_ONE) player1LinesFormed else player2LinesFormed

    fun piecesRemovedBy(player: Player): Int =
        if (player == Player.PLAYER_ONE) player1Removed else player2Removed

    val allPiecesPlaced: Boolean
        get() = player1PiecesToPlace == 0 && player2PiecesToPlace == 0
}

sealed interface GameEvent {
    data class Placed(val nodeId: Int, val player: Player) : GameEvent
    data class Moved(val fromNodeId: Int, val toNodeId: Int, val player: Player) : GameEvent
    data class LineFormed(val lineIds: Set<Int>, val player: Player) : GameEvent
    data class Removed(val nodeId: Int, val owner: Player) : GameEvent
    data class Won(val winner: Player, val reason: WinReason) : GameEvent
}

data class ActionResult(
    val state: LineOfThreeState,
    val events: List<GameEvent>
)
