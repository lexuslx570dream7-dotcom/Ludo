package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.LudoBoardView
import com.example.ui.theme.*

@Composable
fun GameScreen(
    gameState: LudoGameState,
    localUserId: String = "",
    onRollDice: () -> Unit = {},
    onPawnClick: (pawnId: Int) -> Unit = {},
    onActivateMagic: (playerId: String) -> Unit = {},
    onToggleChat: (playerId: String) -> Unit = {},
    onSendChatMessage: (playerId: String, text: String?, emoji: String?) -> Unit = { _, _, _ -> },
    onTogglePlayerMic: (playerId: String) -> Unit = {},
    onToggleMic: () -> Unit = {},
    onLeaveGame: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onLeaveGame() }

    var showExitDialog by remember { mutableStateOf(false) }

    // Dice rotation animation while rolling
    val infiniteTransition = rememberInfiniteTransition(label = "dice_roll")
    val diceRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dice_rot"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GameBgDeep, GameBgSurface, GameBgDeep)
                )
            )
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Game Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { showExitDialog = true },
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFFB71C1C).copy(alpha = 0.85f), CircleShape)
                    .testTag("exit_game_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Leave Match",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Mode & Prize Tag
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = gameState.mode.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Prize: ${gameState.mode.prizePool} 🪙",
                    color = Color(0xFFFFD54F),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Global voice chat status pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F2B44),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E527D))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🎙️", fontSize = 12.sp)
                    Text(
                        text = if (gameState.players.any { it.controlState.micStatus != PlayerMicStatus.MUTED }) "Live Voice" else "Muted",
                        color = Color(0xFF90A4AE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Live Action Log Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .background(Color(0xFF071B2B), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF00E676).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(vertical = 6.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = gameState.logMessage,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }

        // Top Opponents Display (Player index 1 and 2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val p1 = gameState.players.getOrNull(1)
            val p2 = gameState.players.getOrNull(2)

            if (p1 != null) {
                PlayerIsolatedControlCard(
                    player = p1,
                    isActive = gameState.currentPlayer?.id == p1.id,
                    isLocalPlayer = p1.id == localUserId,
                    onActivateMagic = { onActivateMagic(p1.id) },
                    onToggleChat = { onToggleChat(p1.id) },
                    onSendChatMessage = { text, emoji -> onSendChatMessage(p1.id, text, emoji) },
                    onToggleMic = { onTogglePlayerMic(p1.id) },
                    modifier = Modifier.weight(1f, fill = false)
                )
            } else {
                Spacer(modifier = Modifier.width(130.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (p2 != null) {
                PlayerIsolatedControlCard(
                    player = p2,
                    isActive = gameState.currentPlayer?.id == p2.id,
                    isLocalPlayer = p2.id == localUserId,
                    onActivateMagic = { onActivateMagic(p2.id) },
                    onToggleChat = { onToggleChat(p2.id) },
                    onSendChatMessage = { text, emoji -> onSendChatMessage(p2.id, text, emoji) },
                    onToggleMic = { onTogglePlayerMic(p2.id) },
                    modifier = Modifier.weight(1f, fill = false)
                )
            } else {
                Spacer(modifier = Modifier.width(130.dp))
            }
        }

        // Center Interactive Ludo Board
        LudoBoardView(
            gameState = gameState,
            onPawnClick = onPawnClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        // Bottom Players Display (Red me and 4th player)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val me = gameState.players.firstOrNull()
            val p3 = gameState.players.getOrNull(3)

            if (me != null) {
                PlayerIsolatedControlCard(
                    player = me,
                    isActive = gameState.currentPlayer?.id == me.id,
                    isLocalPlayer = true,
                    onActivateMagic = { onActivateMagic(me.id) },
                    onToggleChat = { onToggleChat(me.id) },
                    onSendChatMessage = { text, emoji -> onSendChatMessage(me.id, text, emoji) },
                    onToggleMic = { onTogglePlayerMic(me.id) },
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (p3 != null) {
                PlayerIsolatedControlCard(
                    player = p3,
                    isActive = gameState.currentPlayer?.id == p3.id,
                    isLocalPlayer = p3.id == localUserId,
                    onActivateMagic = { onActivateMagic(p3.id) },
                    onToggleChat = { onToggleChat(p3.id) },
                    onSendChatMessage = { text, emoji -> onSendChatMessage(p3.id, text, emoji) },
                    onToggleMic = { onTogglePlayerMic(p3.id) },
                    modifier = Modifier.weight(1f, fill = false)
                )
            } else {
                Spacer(modifier = Modifier.width(130.dp))
            }
        }

        // Bottom Control Bar: Big 3D Dice Roller and Quick Emojis
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Local Player Magic status shortcut
            val localPlayer = gameState.players.firstOrNull { it.id == localUserId } ?: gameState.players.firstOrNull()
            if (localPlayer != null) {
                val magicStatus = localPlayer.controlState.magicButtonStatus
                val isMagicActive = localPlayer.controlState.isMagicEffectActive
                val cooldown = localPlayer.controlState.magicCooldownSeconds

                Button(
                    onClick = { onActivateMagic(localPlayer.id) },
                    enabled = magicStatus == MagicButtonStatus.READY && !isMagicActive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMagicActive) Color(0xFFFFB300) else Color(0xFF7B1FA2),
                        disabledContainerColor = Color(0xFF37474F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("player_magic_btn_${localPlayer.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(if (isMagicActive) "✨" else "🪄", fontSize = 16.sp)
                        Text(
                            text = when {
                                isMagicActive -> "ACTIVE!"
                                magicStatus == MagicButtonStatus.COOLDOWN -> "${cooldown}s"
                                else -> "MAGIC"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(60.dp))
            }

            // Big 3D Dice Roller
            val isMyTurn = gameState.currentPlayer?.id == localUserId || (gameState.currentPlayer?.isBot == false && localUserId.isEmpty())
            val canRoll = gameState.canRoll && !gameState.isRolling && isMyTurn

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // The 3D Dice
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .shadow(12.dp, RoundedCornerShape(14.dp), spotColor = Color(0xFFFFD54F))
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFFFFF), Color(0xFFE0E0E0))
                            )
                        )
                        .border(2.5.dp, Color(0xFFFFD54F), RoundedCornerShape(14.dp))
                        .rotate(if (gameState.isRolling) diceRotation else 0f)
                        .clickable(enabled = canRoll) { onRollDice() }
                        .testTag("dice_roller_cube"),
                    contentAlignment = Alignment.Center
                ) {
                    if (gameState.isRolling) {
                        Text("🎲", fontSize = 32.sp)
                    } else {
                        DicePipFace(value = gameState.diceValue ?: 6)
                    }
                }

                // Roll Button
                Button(
                    onClick = onRollDice,
                    enabled = canRoll,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00C853),
                        disabledContainerColor = Color(0xFF37474F)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    modifier = Modifier.testTag("roll_dice_btn")
                ) {
                    Text(
                        text = if (isMyTurn && gameState.canRoll) "ROLL" else "WAIT",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    // Exit Confirmation Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Leave Match?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Leaving now will forfeit your match and entry coins.", color = Color(0xFFB0BEC5)) },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onLeaveGame()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Quit Game", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Stay", color = Color(0xFF90A4AE))
                }
            },
            containerColor = Color(0xFF0D253B)
        )
    }

    // Victory Dialog
    if (gameState.isGameOver) {
        AlertDialog(
            onDismissRequest = onLeaveGame,
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("🏆 MATCH OVER! 🏆", color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = gameState.logMessage,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Winner took the prize pool of ${gameState.mode.prizePool} Coins!",
                        color = Color(0xFF00E676),
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onLeaveGame,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    modifier = Modifier.fillMaxWidth().testTag("return_to_lobby_btn")
                ) {
                    Text("CONTINUE", fontWeight = FontWeight.Black, color = Color.White)
                }
            },
            containerColor = Color(0xFF0A2238),
            shape = RoundedCornerShape(22.dp)
        )
    }
}

/**
 * Isolated Player Card with independent Turn Timer, Magic Button, Chat Button, and Mic Toggle
 */
@Composable
private fun PlayerIsolatedControlCard(
    player: LudoPlayer,
    isActive: Boolean,
    isLocalPlayer: Boolean,
    onActivateMagic: () -> Unit,
    onToggleChat: () -> Unit,
    onSendChatMessage: (text: String?, emoji: String?) -> Unit,
    onToggleMic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val control = player.controlState
    val borderColor = when {
        control.isMagicEffectActive -> Color(0xFFFFD54F)
        isActive -> Color(0xFF00E676)
        else -> Color(0xFF1E466B)
    }

    // Pulse animation for active turn or magic
    val infiniteTransition = rememberInfiniteTransition(label = "player_pulse_${player.id}")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column(
        modifier = modifier
            .testTag("player_card_${player.id}"),
        horizontalAlignment = Alignment.Start
    ) {
        // Floating Chat Bubble / Emoji Display
        AnimatedVisibility(
            visible = control.activeEmoji != null || control.lastChatMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF00E676),
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .shadow(6.dp, RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = control.activeEmoji ?: control.lastChatMessage ?: "",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (control.activeEmoji != null) 18.sp else 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // Inline Chat Tray when opened by this player
        AnimatedVisibility(
            visible = control.isChatOpen,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF071B2B),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676)),
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Quick Emojis
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("🔥", "👑", "🎲", "😂", "👏").forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0xFF0F2B44), CircleShape)
                                    .clickable { onSendChatMessage(null, emoji) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 14.sp)
                            }
                        }
                    }
                    // Quick text
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Good Luck!", "Nice Move!").forEach { phrase ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF1E527D), RoundedCornerShape(6.dp))
                                    .clickable { onSendChatMessage(phrase, null) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(phrase, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Main Player Body
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0B2439),
            border = androidx.compose.foundation.BorderStroke(
                width = if (isActive || control.isMagicEffectActive) 2.dp else 1.dp,
                color = if (isActive) borderColor.copy(alpha = glowAlpha) else borderColor
            ),
            shadowElevation = if (isActive) 6.dp else 2.dp
        ) {
            Column(
                modifier = Modifier.padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Header: Avatar, Name, and Turn Timer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Player Avatar
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(player.color.hexColor), CircleShape)
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = player.name.firstOrNull()?.toString() ?: "P",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = player.name,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${player.finishedPawnsCount}/4 Done",
                            color = Color(0xFF90A4AE),
                            fontSize = 9.sp
                        )
                    }

                    // Individual Turn Countdown Timer
                    Box(
                        modifier = Modifier
                            .background(
                                if (isActive) Color(0xFFFF6F00) else Color(0xFF1A344D),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                            .testTag("player_timer_${player.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isActive) "${control.turnTimeRemaining}s" else "15s",
                            color = if (isActive) Color.White else Color(0xFF78909C),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Independent Controls Row for this Player
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Magic Button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                when {
                                    control.isMagicEffectActive -> Color(0xFFFFB300)
                                    control.magicButtonStatus == MagicButtonStatus.READY -> Color(0xFF7B1FA2)
                                    else -> Color(0xFF263238)
                                },
                                CircleShape
                            )
                            .border(
                                1.dp,
                                if (control.isMagicEffectActive) Color.White else Color(0xFFAB47BC),
                                CircleShape
                            )
                            .clickable(enabled = isLocalPlayer && control.magicButtonStatus == MagicButtonStatus.READY && !control.isMagicEffectActive) {
                                onActivateMagic()
                            }
                            .testTag("player_magic_btn_${player.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (control.magicButtonStatus == MagicButtonStatus.COOLDOWN) {
                            Text(
                                text = "${control.magicCooldownSeconds}",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = if (control.isMagicEffectActive) "✨" else "🪄",
                                fontSize = 12.sp
                            )
                        }
                    }

                    // 2. Chat Button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                if (control.isChatOpen) Color(0xFF00E676) else Color(0xFF102A43),
                                CircleShape
                            )
                            .border(1.dp, Color(0xFF1E527D), CircleShape)
                            .clickable(enabled = isLocalPlayer) { onToggleChat() }
                            .testTag("player_chat_btn_${player.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Player Chat",
                            tint = if (control.isChatOpen) Color.Black else Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    // 3. Microphone Toggle Button
                    val isUnmuted = control.micStatus != PlayerMicStatus.MUTED
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                if (isUnmuted) Color(0xFF00C853) else Color(0xFF37474F),
                                CircleShape
                            )
                            .border(1.dp, if (isUnmuted) Color(0xFF69F0AE) else Color(0xFF546E7A), CircleShape)
                            .clickable(enabled = isLocalPlayer) { onToggleMic() }
                            .testTag("player_mic_btn_${player.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isUnmuted) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Player Microphone",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DicePipFace(value: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        when (value) {
            1 -> Box(Modifier.size(10.dp).background(Color.Black, CircleShape))
            2 -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(Modifier.size(8.dp).background(Color.Black, CircleShape).align(Alignment.Start))
                Box(Modifier.size(8.dp).background(Color.Black, CircleShape).align(Alignment.End))
            }
            3 -> Box(modifier = Modifier.fillMaxSize()) {
                Box(Modifier.size(8.dp).background(Color.Black, CircleShape).align(Alignment.TopStart))
                Box(Modifier.size(8.dp).background(Color.Black, CircleShape).align(Alignment.Center))
                Box(Modifier.size(8.dp).background(Color.Black, CircleShape).align(Alignment.BottomEnd))
            }
            4 -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(Modifier.size(8.dp).background(Color.Black, CircleShape))
                    Box(Modifier.size(8.dp).background(Color.Black, CircleShape))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(Modifier.size(8.dp).background(Color.Black, CircleShape))
                    Box(Modifier.size(8.dp).background(Color.Black, CircleShape))
                }
            }
            5 -> Box(modifier = Modifier.fillMaxSize()) {
                Box(Modifier.size(7.dp).background(Color.Black, CircleShape).align(Alignment.TopStart))
                Box(Modifier.size(7.dp).background(Color.Black, CircleShape).align(Alignment.TopEnd))
                Box(Modifier.size(7.dp).background(Color.Black, CircleShape).align(Alignment.Center))
                Box(Modifier.size(7.dp).background(Color.Black, CircleShape).align(Alignment.BottomStart))
                Box(Modifier.size(7.dp).background(Color.Black, CircleShape).align(Alignment.BottomEnd))
            }
            else -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(Modifier.size(7.dp).background(Color.Black, CircleShape))
                    Box(Modifier.size(7.dp).background(Color.Black, CircleShape))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(Modifier.size(7.dp).background(Color.Black, CircleShape))
                    Box(Modifier.size(7.dp).background(Color.Black, CircleShape))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(Modifier.size(7.dp).background(Color.Black, CircleShape))
                    Box(Modifier.size(7.dp).background(Color.Black, CircleShape))
                }
            }
        }
    }
}
