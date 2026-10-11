package com.example.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.base.FirestoreEmulatorTestBase
import com.example.model.*
import com.example.viewmodel.LudoViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LudoPlayerControlsTest : FirestoreEmulatorTestBase() {

    private lateinit var app: Application
    private lateinit var viewModel: LudoViewModel

    @Before
    override fun setUpFirebase() {
        super.setUpFirebase()
        app = ApplicationProvider.getApplicationContext()
        viewModel = LudoViewModel(app)
    }

    @Test
    fun playerControls_eachPlayerHasIsolatedState() {
        val p1 = LudoPlayer(id = "user_1", name = "Alice", color = PlayerColor.RED)
        val p2 = LudoPlayer(id = "user_2", name = "Bob", color = PlayerColor.GREEN)
        val p3 = LudoPlayer(id = "user_3", name = "Charlie", color = PlayerColor.YELLOW)
        val p4 = LudoPlayer(id = "user_4", name = "Diana", color = PlayerColor.BLUE)

        viewModel.initializeGame(GameMode.FOUR_PLAYERS, listOf(p1, p2, p3, p4))
        val state = viewModel.gameState.value

        assertEquals(4, state.players.size)
        // All players have independent controlState objects with matching playerId
        for (player in state.players) {
            assertEquals(player.id, player.controlState.playerId)
            assertEquals(MagicButtonStatus.READY, player.controlState.magicButtonStatus)
            assertEquals(PlayerMicStatus.MUTED, player.controlState.micStatus)
            assertEquals(false, player.controlState.isChatOpen)
            assertEquals(false, player.controlState.isMagicEffectActive)
        }

        // Only first player has turn ACTIVE, others are WAITING
        assertEquals(PlayerTurnStatus.ACTIVE, state.players[0].controlState.turnStatus)
        assertEquals(PlayerTurnStatus.WAITING, state.players[1].controlState.turnStatus)
        assertEquals(PlayerTurnStatus.WAITING, state.players[2].controlState.turnStatus)
        assertEquals(PlayerTurnStatus.WAITING, state.players[3].controlState.turnStatus)
    }

    @Test
    fun magicButton_activatingPlayer1_affectsOnlyPlayer1() {
        val myUserId = viewModel.repository.userAccount.value.userId
        val p1 = LudoPlayer(id = myUserId, name = "Me", color = PlayerColor.RED)
        val p2 = LudoPlayer(id = "user_2", name = "Bob", color = PlayerColor.GREEN)
        val p3 = LudoPlayer(id = "user_3", name = "Charlie", color = PlayerColor.YELLOW)
        val p4 = LudoPlayer(id = "user_4", name = "Diana", color = PlayerColor.BLUE)

        viewModel.initializeGame(GameMode.FOUR_PLAYERS, listOf(p1, p2, p3, p4))

        // Activate Magic on Player 1
        viewModel.activatePlayerMagic(myUserId)
        val state = viewModel.gameState.value

        val updatedP1 = state.getPlayer(myUserId)!!
        val updatedP2 = state.getPlayer("user_2")!!
        val updatedP3 = state.getPlayer("user_3")!!
        val updatedP4 = state.getPlayer("user_4")!!

        // Only Player 1's magic should be active
        assertEquals(MagicButtonStatus.ACTIVE, updatedP1.controlState.magicButtonStatus)
        assertTrue(updatedP1.controlState.isMagicEffectActive)
        assertEquals("MAGIC_ACTIVE_$myUserId", updatedP1.controlState.magicAnimationTag)

        // Player 2, 3, and 4 must remain unaffected
        assertEquals(MagicButtonStatus.READY, updatedP2.controlState.magicButtonStatus)
        assertFalse(updatedP2.controlState.isMagicEffectActive)
        assertNull(updatedP2.controlState.magicAnimationTag)

        assertEquals(MagicButtonStatus.READY, updatedP3.controlState.magicButtonStatus)
        assertFalse(updatedP3.controlState.isMagicEffectActive)

        assertEquals(MagicButtonStatus.READY, updatedP4.controlState.magicButtonStatus)
        assertFalse(updatedP4.controlState.isMagicEffectActive)
    }

    @Test
    fun microphoneControl_togglingPlayer1_doesNotAffectOtherPlayers() {
        val myUserId = viewModel.repository.userAccount.value.userId
        val p1 = LudoPlayer(id = myUserId, name = "Me", color = PlayerColor.RED)
        val p2 = LudoPlayer(id = "user_2", name = "Bob", color = PlayerColor.GREEN)
        val p3 = LudoPlayer(id = "user_3", name = "Charlie", color = PlayerColor.YELLOW)

        viewModel.initializeGame(GameMode.FOUR_PLAYERS, listOf(p1, p2, p3))

        // Initially all muted
        assertEquals(PlayerMicStatus.MUTED, viewModel.gameState.value.getPlayer(myUserId)!!.controlState.micStatus)
        assertEquals(PlayerMicStatus.MUTED, viewModel.gameState.value.getPlayer("user_2")!!.controlState.micStatus)

        // Toggle Player 1 mic ON
        viewModel.togglePlayerMic(myUserId)

        val stateAfterOn = viewModel.gameState.value
        assertEquals(PlayerMicStatus.UNMUTED, stateAfterOn.getPlayer(myUserId)!!.controlState.micStatus)
        assertFalse(stateAfterOn.getPlayer(myUserId)!!.isMuted)

        // Player 2 and 3 must still be MUTED!
        assertEquals(PlayerMicStatus.MUTED, stateAfterOn.getPlayer("user_2")!!.controlState.micStatus)
        assertTrue(stateAfterOn.getPlayer("user_2")!!.isMuted)
        assertEquals(PlayerMicStatus.MUTED, stateAfterOn.getPlayer("user_3")!!.controlState.micStatus)
        assertTrue(stateAfterOn.getPlayer("user_3")!!.isMuted)

        // Toggle Player 1 mic OFF again
        viewModel.togglePlayerMic(myUserId)
        val stateAfterOff = viewModel.gameState.value
        assertEquals(PlayerMicStatus.MUTED, stateAfterOff.getPlayer(myUserId)!!.controlState.micStatus)
        assertTrue(stateAfterOff.getPlayer(myUserId)!!.isMuted)
        // Others still unaffected
        assertEquals(PlayerMicStatus.MUTED, stateAfterOff.getPlayer("user_2")!!.controlState.micStatus)
    }

    @Test
    fun chatControl_togglingAndSending_isolatedToPlayer() {
        val myUserId = viewModel.repository.userAccount.value.userId
        val p1 = LudoPlayer(id = myUserId, name = "Me", color = PlayerColor.RED)
        val p2 = LudoPlayer(id = "user_2", name = "Bob", color = PlayerColor.GREEN)

        viewModel.initializeGame(GameMode.TWO_PLAYERS, listOf(p1, p2))

        // Toggle chat for Player 1
        viewModel.togglePlayerChat(myUserId)

        val stateOpen = viewModel.gameState.value
        assertTrue(stateOpen.getPlayer(myUserId)!!.controlState.isChatOpen)
        // Player 2 chat must NOT open
        assertFalse(stateOpen.getPlayer("user_2")!!.controlState.isChatOpen)

        // Send a message from Player 1
        viewModel.sendPlayerChatMessage(myUserId, text = "Nice Move!", emoji = "🔥")

        val stateSent = viewModel.gameState.value
        val p1Ctrl = stateSent.getPlayer(myUserId)!!.controlState
        val p2Ctrl = stateSent.getPlayer("user_2")!!.controlState

        assertFalse(p1Ctrl.isChatOpen)
        assertEquals("Nice Move!", p1Ctrl.lastChatMessage)
        assertEquals("🔥", p1Ctrl.activeEmoji)

        // Player 2 must have no message or emoji
        assertNull(p2Ctrl.lastChatMessage)
        assertNull(p2Ctrl.activeEmoji)
    }

    @Test
    fun turnTimer_onlyActivePlayerTimerBelongsToActiveTurn() {
        val p1 = LudoPlayer(id = "user_1", name = "Alice", color = PlayerColor.RED)
        val p2 = LudoPlayer(id = "user_2", name = "Bob", color = PlayerColor.GREEN)

        viewModel.initializeGame(GameMode.TWO_PLAYERS, listOf(p1, p2))
        val state = viewModel.gameState.value

        // Player 1 is active player
        assertEquals("user_1", state.currentPlayer?.id)
        assertEquals(PlayerTurnStatus.ACTIVE, state.getPlayer("user_1")!!.controlState.turnStatus)
        assertEquals(PlayerTurnStatus.WAITING, state.getPlayer("user_2")!!.controlState.turnStatus)
    }
}
