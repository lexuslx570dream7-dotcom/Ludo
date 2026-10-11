package com.example

import com.example.model.*
import com.example.viewmodel.LudoViewModel
import com.example.repository.LudoRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PerPlayerIsolatedControlsTest {

    private lateinit var viewModel: LudoViewModel
    private val p1Id = "user_p1"
    private val p2Id = "user_p2"
    private val p3Id = "user_p3"
    private val p4Id = "user_p4"

    @Before
    fun setUp() {
        val repository = LudoRepository()
        viewModel = LudoViewModel(repository)
    }

    @Test
    fun testIndependentMagicButtonIsolation() {
        val players = listOf(
            LudoPlayer(id = p1Id, name = "Player 1", color = PlayerColor.RED),
            LudoPlayer(id = p2Id, name = "Player 2", color = PlayerColor.GREEN)
        )
        viewModel.initializeGame(GameMode.TWO_PLAYER, players)

        val initialP1 = viewModel.gameState.value.getPlayer(p1Id)!!
        val initialP2 = viewModel.gameState.value.getPlayer(p2Id)!!
        assertEquals(MagicButtonStatus.READY, initialP1.controlState.magicButtonStatus)
        assertEquals(MagicButtonStatus.READY, initialP2.controlState.magicButtonStatus)

        // Player 1 activates magic
        viewModel.activatePlayerMagic(p1Id)

        val updatedP1 = viewModel.gameState.value.getPlayer(p1Id)!!
        val updatedP2 = viewModel.gameState.value.getPlayer(p2Id)!!

        // Only Player 1 should be active / on cooldown
        assertTrue(updatedP1.controlState.isMagicEffectActive)
        assertEquals(MagicButtonStatus.COOLDOWN, updatedP1.controlState.magicButtonStatus)

        // Player 2's magic button MUST remain strictly unaffected
        assertFalse(updatedP2.controlState.isMagicEffectActive)
        assertEquals(MagicButtonStatus.READY, updatedP2.controlState.magicButtonStatus)
        assertEquals(0, updatedP2.controlState.magicCooldownSeconds)
    }

    @Test
    fun testIndependentChatButtonAndMessageIsolation() {
        val players = listOf(
            LudoPlayer(id = p1Id, name = "Player 1", color = PlayerColor.RED),
            LudoPlayer(id = p2Id, name = "Player 2", color = PlayerColor.GREEN),
            LudoPlayer(id = p3Id, name = "Player 3", color = PlayerColor.YELLOW),
            LudoPlayer(id = p4Id, name = "Player 4", color = PlayerColor.BLUE)
        )
        viewModel.initializeGame(GameMode.FOUR_PLAYER, players)

        // Toggle chat only for Player 2
        viewModel.togglePlayerChat(p2Id)

        val p1 = viewModel.gameState.value.getPlayer(p1Id)!!
        val p2 = viewModel.gameState.value.getPlayer(p2Id)!!
        val p3 = viewModel.gameState.value.getPlayer(p3Id)!!
        val p4 = viewModel.gameState.value.getPlayer(p4Id)!!

        assertFalse(p1.controlState.isChatOpen)
        assertTrue(p2.controlState.isChatOpen)
        assertFalse(p3.controlState.isChatOpen)
        assertFalse(p4.controlState.isChatOpen)

        // Send chat emoji from Player 2
        viewModel.sendPlayerChatMessage(p2Id, emoji = "🔥")

        val p1After = viewModel.gameState.value.getPlayer(p1Id)!!
        val p2After = viewModel.gameState.value.getPlayer(p2Id)!!
        val p3After = viewModel.gameState.value.getPlayer(p3Id)!!

        assertNull(p1After.controlState.activeEmoji)
        assertEquals("🔥", p2After.controlState.activeEmoji)
        assertNull(p3After.controlState.activeEmoji)
    }

    @Test
    fun testIndependentMicrophoneControls() {
        val players = listOf(
            LudoPlayer(id = p1Id, name = "Player 1", color = PlayerColor.RED),
            LudoPlayer(id = p2Id, name = "Player 2", color = PlayerColor.GREEN)
        )
        viewModel.initializeGame(GameMode.TWO_PLAYER, players)

        // Both start muted by default
        assertEquals(PlayerMicStatus.MUTED, viewModel.gameState.value.getPlayer(p1Id)!!.controlState.micStatus)
        assertEquals(PlayerMicStatus.MUTED, viewModel.gameState.value.getPlayer(p2Id)!!.controlState.micStatus)

        // Player 1 unmutes
        viewModel.togglePlayerMic(p1Id)

        val p1 = viewModel.gameState.value.getPlayer(p1Id)!!
        val p2 = viewModel.gameState.value.getPlayer(p2Id)!!

        assertEquals(PlayerMicStatus.UNMUTED, p1.controlState.micStatus)
        assertEquals(PlayerMicStatus.MUTED, p2.controlState.micStatus)

        // Player 1 mutes back
        viewModel.togglePlayerMic(p1Id)
        assertEquals(PlayerMicStatus.MUTED, viewModel.gameState.value.getPlayer(p1Id)!!.controlState.micStatus)
        assertEquals(PlayerMicStatus.MUTED, viewModel.gameState.value.getPlayer(p2Id)!!.controlState.micStatus)
    }

    @Test
    fun testIndependentTurnStateIsolation() {
        val players = listOf(
            LudoPlayer(id = p1Id, name = "Player 1", color = PlayerColor.RED),
            LudoPlayer(id = p2Id, name = "Player 2", color = PlayerColor.GREEN)
        )
        viewModel.initializeGame(GameMode.TWO_PLAYER, players)

        val state = viewModel.gameState.value
        assertEquals(p1Id, state.currentPlayer?.id)

        val p1 = state.getPlayer(p1Id)!!
        val p2 = state.getPlayer(p2Id)!!

        assertEquals(PlayerTurnStatus.ACTIVE, p1.controlState.turnStatus)
        assertEquals(PlayerTurnStatus.WAITING, p2.controlState.turnStatus)
    }
}
