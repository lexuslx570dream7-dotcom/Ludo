package com.example

import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPawnCanLeaveBaseWithSix() {
        val basePawn = Pawn(id = 0, color = PlayerColor.RED, step = -1)
        assertFalse(LudoLogic.canPawnMove(basePawn, 5))
        assertTrue(LudoLogic.canPawnMove(basePawn, 6))
    }

    @Test
    fun testPawnMoveExecution() {
        val player = LudoPlayer(
            id = "p1",
            name = "TestPlayer",
            color = PlayerColor.RED,
            pawns = listOf(Pawn(id = 0, color = PlayerColor.RED, step = -1))
        )
        val result = LudoLogic.executeMove(
            players = listOf(player),
            activePlayer = player,
            pawnId = 0,
            diceRoll = 6
        )
        val updatedPawn = result.updatedPlayers.first().pawns.first()
        assertEquals(0, updatedPawn.step)
        assertTrue(result.awardsExtraTurn)
    }

    @Test
    fun testSafeTiles() {
        assertTrue(LudoLogic.SAFE_TILES.contains(0))
        assertTrue(LudoLogic.SAFE_TILES.contains(8))
        assertTrue(LudoLogic.SAFE_TILES.contains(13))
        assertTrue(LudoLogic.SAFE_TILES.contains(26))
        assertTrue(LudoLogic.SAFE_TILES.contains(39))
    }
}
