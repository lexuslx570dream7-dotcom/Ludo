package com.example.model

object LudoLogic {

    // Standard starting positions on the 52-tile circuit
    fun getStartOffset(color: PlayerColor): Int = when (color) {
        PlayerColor.RED -> 0
        PlayerColor.GREEN -> 13
        PlayerColor.YELLOW -> 26
        PlayerColor.BLUE -> 39
    }

    // Safe tiles on the 52-tile circuit where no pawn can be captured
    val SAFE_TILES: Set<Int> = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    fun getBoardCircuitIndex(color: PlayerColor, step: Int): Int? {
        if (step in 0..50) {
            return (getStartOffset(color) + step) % 52
        }
        return null // in base or in home stretch
    }

    fun canPawnMove(pawn: Pawn, diceRoll: Int): Boolean {
        if (pawn.isFinished) return false
        if (pawn.isInBase) {
            return diceRoll == 6
        }
        val targetStep = pawn.step + diceRoll
        return targetStep <= 56
    }

    fun getMovablePawnIds(player: LudoPlayer, diceRoll: Int): List<Int> {
        return player.pawns.filter { canPawnMove(it, diceRoll) }.map { it.id }
    }

    data class MoveResult(
        val updatedPlayers: List<LudoPlayer>,
        val capturedOpponent: LudoPlayer?,
        val pawnFinished: Boolean,
        val awardsExtraTurn: Boolean,
        val logMessage: String
    )

    fun executeMove(
        players: List<LudoPlayer>,
        activePlayer: LudoPlayer,
        pawnId: Int,
        diceRoll: Int,
        isTeamMode: Boolean = false
    ): MoveResult {
        val pawn = activePlayer.pawns.firstOrNull { it.id == pawnId }
            ?: return MoveResult(players, null, false, false, "Invalid move")

        val newStep = if (pawn.isInBase) {
            if (diceRoll == 6) 0 else -1
        } else {
            pawn.step + diceRoll
        }

        if (newStep > 56) {
            return MoveResult(players, null, false, false, "Cannot overshoot Home")
        }

        val updatedPawn = pawn.copy(step = newStep)
        val updatedActivePawns = activePlayer.pawns.map {
            if (it.id == pawnId) updatedPawn else it
        }
        val updatedActivePlayer = activePlayer.copy(pawns = updatedActivePawns)

        var capturedPlayer: LudoPlayer? = null
        var log = "${activePlayer.name} rolled $diceRoll and moved pawn #${pawnId + 1}"

        val circuitPos = getBoardCircuitIndex(activePlayer.color, newStep)
        val isSafe = circuitPos != null && SAFE_TILES.contains(circuitPos)

        val updatedPlayers = players.map { player ->
            if (player.id == activePlayer.id) {
                updatedActivePlayer
            } else {
                // Check if any opponent pawn is captured on the same circuit tile
                val isTeammate = isTeamMode && (
                    (activePlayer.color == PlayerColor.RED && player.color == PlayerColor.YELLOW) ||
                    (activePlayer.color == PlayerColor.YELLOW && player.color == PlayerColor.RED) ||
                    (activePlayer.color == PlayerColor.GREEN && player.color == PlayerColor.BLUE) ||
                    (activePlayer.color == PlayerColor.BLUE && player.color == PlayerColor.GREEN)
                )

                if (!isSafe && circuitPos != null && !isTeammate) {
                    var capturedInThisPlayer = false
                    val modifiedPawns = player.pawns.map { oppPawn ->
                        val oppCircuitPos = getBoardCircuitIndex(oppPawn.color, oppPawn.step)
                        if (oppCircuitPos == circuitPos && oppPawn.step in 0..50) {
                            capturedInThisPlayer = true
                            oppPawn.copy(step = -1) // Send back to base
                        } else {
                            oppPawn
                        }
                    }
                    if (capturedInThisPlayer) {
                        capturedPlayer = player
                        player.copy(pawns = modifiedPawns)
                    } else {
                        player
                    }
                } else {
                    player
                }
            }
        }

        val pawnReachedHome = updatedPawn.step == 56
        if (pawnReachedHome) {
            log += " into HOME! ⭐"
        }

        if (capturedPlayer != null) {
            log += " - Captured ${capturedPlayer?.name}'s pawn! 🎯"
        }

        val extraTurn = diceRoll == 6 || capturedPlayer != null || pawnReachedHome

        return MoveResult(
            updatedPlayers = updatedPlayers,
            capturedOpponent = capturedPlayer,
            pawnFinished = pawnReachedHome,
            awardsExtraTurn = extraTurn,
            logMessage = log
        )
    }
}
