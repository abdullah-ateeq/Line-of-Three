package com.example.lineofthree.game

import com.example.lineofthree.game.Player.PLAYER_ONE
import com.example.lineofthree.game.Player.PLAYER_TWO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    private fun movementState(
        p1: List<Int>,
        p2: List<Int>,
        current: Player = PLAYER_ONE
    ) = LineOfThreeState(
        pieces = p1.associateWith { PLAYER_ONE } + p2.associateWith { PLAYER_TWO },
        currentPlayer = current,
        phase = GamePhase.MOVEMENT,
        player1PiecesToPlace = 0,
        player2PiecesToPlace = 0
    )

    private fun lineId(vararg nodes: Int) =
        BoardRules.validLines.indexOfFirst { it.toSet() == nodes.toSet() }

    @Test
    fun boardDefinitionIsConsistent() {
        assertEquals(16, BoardRules.nodes.size)
        BoardRules.adjacency.forEach { (a, ns) -> ns.forEach { b -> assertTrue(a in BoardRules.neighbors(b)) } }
        BoardRules.validLines.forEach { (a, b, c) ->
            assertTrue(BoardRules.isAdjacent(a, b) && BoardRules.isAdjacent(b, c))
        }
    }

    @Test
    fun initialSetup() {
        val s = GameEngine.newGame()
        assertTrue(s.pieces.isEmpty())
        assertEquals(PLAYER_ONE, s.currentPlayer)
        assertEquals(GamePhase.PLACEMENT, s.phase)
        assertEquals(6, s.player1PiecesToPlace)
        assertEquals(6, s.player2PiecesToPlace)
        assertNull(s.winner)
    }

    @Test
    fun legalPlacement() {
        val s = GameEngine.placePiece(GameEngine.newGame(), 0)!!.state
        assertEquals(PLAYER_ONE, s.pieces[0])
        assertEquals(5, s.player1PiecesToPlace)
        assertEquals(PLAYER_TWO, s.currentPlayer)
        assertEquals(1, s.turns)
    }

    @Test
    fun occupiedNodeRejected() {
        val s = GameEngine.placePiece(GameEngine.newGame(), 0)!!.state
        assertNull(GameEngine.placePiece(s, 0))
    }

    @Test
    fun lineOfThreeDetected() {
        val pieces = mapOf(0 to PLAYER_ONE, 1 to PLAYER_ONE, 2 to PLAYER_ONE, 9 to PLAYER_ONE)
        assertEquals(setOf(lineId(0, 1, 2)), GameEngine.getCompletedLineIds(pieces, PLAYER_ONE))
        assertTrue(GameEngine.getCompletedLineIds(pieces, PLAYER_TWO).isEmpty())
    }

    @Test
    fun preExistingLineGrantsNoRemoval() {
        val s = movementState(p1 = listOf(0, 1, 2, 8), p2 = listOf(4, 5, 12))
        val r = GameEngine.movePiece(s, 8, 15)!!.state
        assertEquals(GamePhase.MOVEMENT, r.phase)
        assertEquals(PLAYER_TWO, r.currentPlayer)
    }

    @Test
    fun newLineEntersRemoveMode() {
        val s = movementState(p1 = listOf(0, 1, 3), p2 = listOf(5, 12, 14))
        val r = GameEngine.movePiece(s, 3, 2)!!
        assertEquals(GamePhase.REMOVE, r.state.phase)
        assertEquals(PLAYER_ONE, r.state.currentPlayer)
        assertEquals(setOf(lineId(0, 1, 2)), r.state.newLineIds)
        assertTrue(r.events.any { it is GameEvent.LineFormed })
    }

    @Test
    fun reformedLineGrantsRemovalAgain() {
        val s = movementState(p1 = listOf(0, 1, 2), p2 = listOf(5, 12, 14, 13))
        val broken = GameEngine.movePiece(s, 2, 3)!!.state
        assertEquals(GamePhase.MOVEMENT, broken.phase)
        val p2Moved = GameEngine.movePiece(broken, 14, 15)!!.state
        assertEquals(GamePhase.REMOVE, GameEngine.movePiece(p2Moved, 3, 2)!!.state.phase)
    }

    @Test
    fun doubleLineGrantsOnlyOneRemoval() {
        val s = movementState(p1 = listOf(0, 1, 3, 4, 10), p2 = listOf(6, 12, 14, 15))
        val formed = GameEngine.movePiece(s, 10, 2)!!.state
        assertEquals(GamePhase.REMOVE, formed.phase)
        assertEquals(2, formed.newLineIds.size)
        assertEquals(2, formed.player1LinesFormed)

        val afterRemoval = GameEngine.removeOpponentPiece(formed, 6)!!.state
        assertEquals(GamePhase.MOVEMENT, afterRemoval.phase)
        assertEquals(PLAYER_TWO, afterRemoval.currentPlayer)
        assertNull(GameEngine.removeOpponentPiece(afterRemoval, 12))
        assertEquals(1, afterRemoval.player1Removed)
    }

    @Test
    fun protectedLinePiecesCannotBeRemoved() {
        val s = movementState(p1 = listOf(0, 1, 3), p2 = listOf(8, 9, 10, 12))
        val formed = GameEngine.movePiece(s, 3, 2)!!.state
        assertEquals(setOf(12), formed.removableNodeIds)
        assertNull(GameEngine.removeOpponentPiece(formed, 9))
        assertNull(GameEngine.removeOpponentPiece(formed, 0)) // own piece
        assertNotNull(GameEngine.removeOpponentPiece(formed, 12))
    }

    @Test
    fun allOpponentPiecesInLinesAreRemovable() {
        val pieces = mapOf(8 to PLAYER_TWO, 9 to PLAYER_TWO, 10 to PLAYER_TWO, 11 to PLAYER_TWO, 12 to PLAYER_TWO)
        assertEquals(setOf(8, 9, 10, 11, 12), GameEngine.getRemovableOpponentNodes(pieces, PLAYER_TWO))
    }

    @Test
    fun transitionsToMovementAfterAllPlacements() {
        val order = listOf(0, 1, 2, 3, 4, 5, 6, 7, 9, 11, 13, 15)
        var s = GameEngine.newGame()
        order.forEach { node ->
            assertEquals(GamePhase.PLACEMENT, s.phase)
            s = GameEngine.placePiece(s, node)!!.state
        }
        assertEquals(GamePhase.MOVEMENT, s.phase)
        assertEquals(PLAYER_ONE, s.currentPlayer)
        assertEquals(0, s.player1PiecesToPlace + s.player2PiecesToPlace)
    }

    @Test
    fun legalAdjacentMovement() {
        val s = movementState(p1 = listOf(0, 4, 6), p2 = listOf(2, 12, 14))
        val selected = GameEngine.selectPiece(s, 0)!!
        assertEquals(setOf(1, 7, 8), selected.legalDestinationIds)
        val moved = GameEngine.movePiece(selected, 0, 8)!!.state
        assertEquals(PLAYER_ONE, moved.pieces[8])
        assertFalse(0 in moved.pieces)
    }

    @Test
    fun illegalMovementRejected() {
        val s = movementState(p1 = listOf(0, 4, 6), p2 = listOf(1, 12, 14))
        assertNull(GameEngine.movePiece(s, 0, 10))       // not adjacent
        assertNull(GameEngine.movePiece(s, 0, 1))        // occupied
        assertNull(GameEngine.movePiece(s, 1, 9))        // opponent piece
        assertNull(GameEngine.selectPiece(s, 1))         // cannot select opponent
    }

    @Test
    fun winWhenOpponentReducedBelowThree() {
        val s = movementState(p1 = listOf(0, 1, 3), p2 = listOf(5, 12, 14))
        val formed = GameEngine.movePiece(s, 3, 2)!!.state
        val won = GameEngine.removeOpponentPiece(formed, 12)!!.state
        assertEquals(PLAYER_ONE, won.winner)
        assertEquals(WinReason.REDUCED, won.winReason)
        assertNull(GameEngine.movePiece(won, 2, 3))
    }

    @Test
    fun blockadeWin() {
        val s = movementState(p1 = listOf(3, 7, 8, 9, 10, 13), p2 = listOf(0, 1, 2))
        val r = GameEngine.movePiece(s, 13, 12)!!.state
        assertEquals(PLAYER_ONE, r.winner)
        assertEquals(WinReason.BLOCKED, r.winReason)
    }

    @Test
    fun noBlockadeDuringPlacement() {
        val s = LineOfThreeState(
            pieces = mapOf(0 to PLAYER_TWO, 1 to PLAYER_ONE, 7 to PLAYER_ONE),
            currentPlayer = PLAYER_ONE,
            player1PiecesToPlace = 4,
            player2PiecesToPlace = 5
        )
        val r = GameEngine.placePiece(s, 8)!!.state
        assertNull(r.winner)
        assertEquals(GamePhase.PLACEMENT, r.phase)
    }

    @Test
    fun undoRevertsMoveAndRemovalAsOneTurn() {
        val start = movementState(p1 = listOf(0, 1, 3), p2 = listOf(5, 12, 14, 15))
        val session = GameSession(start)
        session.movePiece(3, 2)
        assertEquals(GamePhase.REMOVE, session.state.phase)
        session.removeOpponentPiece(5)
        assertEquals(PLAYER_TWO, session.state.currentPlayer)

        assertTrue(session.undo())
        assertEquals(start, session.state)
        assertFalse(session.canUndo)
    }

    @Test
    fun inputRejectedWhileAnimating() {
        val session = GameSession()
        session.setAnimating(true)
        assertNull(session.tap(0))
        assertTrue(session.state.pieces.isEmpty())
    }
}
