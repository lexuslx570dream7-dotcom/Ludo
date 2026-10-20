package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.*
import com.example.ui.components.LudoBoardView
import com.example.ui.theme.*

@Composable
fun GameScreen(
    gameState: LudoGameState,
    localUserId: String = "",
    soundEnabled: Boolean = true,
    coinBalance: Long = 10000L,
    gemBalance: Long = 50L,
    spectatorCount: Int = 0,
    onRollDice: () -> Unit = {},
    onPawnClick: (pawnId: Int) -> Unit = {},
    onActivateMagic: (playerId: String) -> Unit = {},
    onToggleChat: (playerId: String) -> Unit = {},
    onSendChatMessage: (playerId: String, text: String?, emoji: String?) -> Unit = { _, _, _ -> },
    onTogglePlayerMic: (playerId: String) -> Unit = {},
    onToggleMic: () -> Unit = {},
    onToggleSound: () -> Unit = {},
    onSubmitFeedback: (String) -> Unit = {},
    onSendGift: (targetPlayerId: String, giftName: String, giftEmoji: String, cost: Long) -> Boolean = { _, _, _, _ -> true },
    onLeaveGame: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onLeaveGame() }

    // Dialog states
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showLeaderboardDialog by remember { mutableStateOf(false) }
    var showShopDialog by remember { mutableStateOf(false) }
    var showMutePlayersDialog by remember { mutableStateOf(false) }
    var selectedGiftPlayer by remember { mutableStateOf<LudoPlayer?>(null) }
    var showEmojiSheet by remember { mutableStateOf(false) }
    var showTextChatSheet by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Transient in-game feedback toast
    var toastMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            kotlinx.coroutines.delay(2500)
            toastMessage = null
        }
    }

    // Determine players
    val players = gameState.players
    val localPlayer = players.firstOrNull { it.id == localUserId } ?: players.firstOrNull()
    val isMyTurn = gameState.currentPlayer?.id == localPlayer?.id
    val canRoll = gameState.canRoll && !gameState.isRolling && isMyTurn

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

    // Player slots:
    // Top-Left: Player 2 (Ali)
    // Top-Right: Player 3 (Ayan)
    // Bottom-Left: Local Player 1 (Ayan KN)
    // Bottom-Right: Player 4 (PabLou)
    val pTopLeft = players.getOrNull(1)
    val pTopRight = players.getOrNull(2)
    val pBottomLeft = localPlayer
    val pBottomRight = players.getOrNull(3)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF281C3E),
                        Color(0xFF1E1430),
                        Color(0xFF150D24)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ==========================================
            // 1. TOP BAR
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Settings Gear & Trophy/Leaderboard
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Settings Icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF78909C), Color(0xFF37474F))
                                )
                            )
                            .border(1.dp, Color(0xFFB0BEC5), CircleShape)
                            .clickable { showSettingsDialog = true }
                            .testTag("game_settings_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Trophy / Leaderboard Icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFFFD54F), Color(0xFFFFA000))
                                )
                            )
                            .border(1.5.dp, Color(0xFFFFF9C4), CircleShape)
                            .clickable { showLeaderboardDialog = true }
                            .testTag("game_leaderboard_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏆", fontSize = 18.sp)
                    }
                }

                // Center: Spectator Count Pill
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF130922).copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFF5E4588), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("👁️", fontSize = 12.sp)
                        Text(
                            text = "Spectator",
                            color = Color(0xFFD1C4E9),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$spectatorCount",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Right: Shop / Inventory Gift Bag
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF00BFA5), Color(0xFF00695C))
                            )
                        )
                        .border(1.5.dp, Color(0xFF64FFDA), CircleShape)
                        .clickable { showShopDialog = true }
                        .testTag("game_shop_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛍️", fontSize = 18.sp)
                }
            }

            // ==========================================
            // 2. TOP PLAYER PROFILES (Opponents)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Top-Left Player Profile (e.g. Ali)
                if (pTopLeft != null) {
                    PlayerProfileSection(
                        player = pTopLeft,
                        isActive = gameState.currentPlayer?.id == pTopLeft.id,
                        isTop = true,
                        isAlignLeft = true,
                        onGiftClick = { selectedGiftPlayer = pTopLeft },
                        onMicToggle = { onTogglePlayerMic(pTopLeft.id) }
                    )
                } else {
                    Spacer(modifier = Modifier.width(130.dp))
                }

                // Top-Right Player Profile (e.g. Ayan)
                if (pTopRight != null) {
                    PlayerProfileSection(
                        player = pTopRight,
                        isActive = gameState.currentPlayer?.id == pTopRight.id,
                        isTop = true,
                        isAlignLeft = false,
                        onGiftClick = { selectedGiftPlayer = pTopRight },
                        onMicToggle = { onTogglePlayerMic(pTopRight.id) }
                    )
                } else {
                    Spacer(modifier = Modifier.width(130.dp))
                }
            }

            // ==========================================
            // 3. CENTER LUDO BOARD
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                LudoBoardView(
                    gameState = gameState,
                    onPawnClick = onPawnClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                )
            }

            // ==========================================
            // 4. BOTTOM ACTIVE PLAYERS & DICE ROLLER
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bottom-Left Profile (Local Player)
                if (pBottomLeft != null) {
                    PlayerProfileSection(
                        player = pBottomLeft,
                        isActive = gameState.currentPlayer?.id == pBottomLeft.id,
                        isTop = false,
                        isAlignLeft = true,
                        onGiftClick = { selectedGiftPlayer = pBottomLeft },
                        onMicToggle = { onTogglePlayerMic(pBottomLeft.id) }
                    )
                } else {
                    Spacer(modifier = Modifier.width(90.dp))
                }

                // Center Dice Roll Area
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Square Button displaying Crown when waiting, or interactive 3D Dice when active
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x66000000))
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFEDE7F6))
                                )
                            )
                            .border(2.dp, if (canRoll) Color(0xFF00E676) else Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                            .clickable(enabled = canRoll) { onRollDice() }
                            .testTag("center_dice_roll_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!canRoll && !gameState.isRolling) {
                            // Crown icon when it is not the player's turn
                            Text(
                                text = "👑",
                                fontSize = 30.sp
                            )
                        } else {
                            // Active 3D Dice
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(if (gameState.isRolling) diceRotation else 0f),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_3d_dice_glossy),
                                    contentDescription = "Roll Dice",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Rolled pip overlay
                                if (gameState.diceValue != null && !gameState.isRolling) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.BottomEnd)
                                            .offset(x = 2.dp, y = 2.dp)
                                            .background(Color(0xFFFF1744), CircleShape)
                                            .border(1.5.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${gameState.diceValue}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Circular reload / turns remaining indicator
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1430))
                            .border(1.5.dp, Color(0xFF5E4588), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("🔄", fontSize = 11.sp)
                            Text(
                                text = if (gameState.canRoll && gameState.diceValue == null) "1" else "0",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text("💎", fontSize = 9.sp)
                        }
                    }
                }

                // Bottom-Right Profile (Player 4)
                if (pBottomRight != null) {
                    PlayerProfileSection(
                        player = pBottomRight,
                        isActive = gameState.currentPlayer?.id == pBottomRight.id,
                        isTop = false,
                        isAlignLeft = false,
                        onGiftClick = { selectedGiftPlayer = pBottomRight },
                        onMicToggle = { onTogglePlayerMic(pBottomRight.id) }
                    )
                } else {
                    Spacer(modifier = Modifier.width(90.dp))
                }
            }

            // ==========================================
            // 5. VERY BOTTOM CONTROLS (Mic, Emoji, Chat)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp, top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Microphone Mute Toggle Button
                val isMyMicMuted = localPlayer?.controlState?.micStatus == PlayerMicStatus.MUTED
                Box(
                    modifier = Modifier
                        .size(width = 66.dp, height = 40.dp)
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF545163), Color(0xFF383547))
                            )
                        )
                        .border(1.dp, Color(0xFF79748D), RoundedCornerShape(20.dp))
                        .clickable {
                            onToggleMic()
                            toastMessage = if (isMyMicMuted) "Microphone Unmuted" else "Microphone Muted"
                        }
                        .testTag("bottom_my_mic_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMyMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Toggle My Mic",
                        tint = if (isMyMicMuted) Color(0xFFFF8A80) else Color(0xFF69F0AE),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Mute Other Players Control Button
                Box(
                    modifier = Modifier
                        .size(width = 54.dp, height = 40.dp)
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF4527A0), Color(0xFF311B92))
                            )
                        )
                        .border(1.dp, Color(0xFF7E57C2), RoundedCornerShape(20.dp))
                        .clickable { showMutePlayersDialog = true }
                        .testTag("manage_player_mutes_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeMute,
                        contentDescription = "Mute Players",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Emoji Reaction Button
                Box(
                    modifier = Modifier
                        .size(width = 72.dp, height = 42.dp)
                        .shadow(8.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFFFFB300))
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFD54F), Color(0xFFFFB300))
                            )
                        )
                        .border(1.dp, Color(0xFFFFF9C4), RoundedCornerShape(22.dp))
                        .clickable { showEmojiSheet = true }
                        .testTag("bottom_emoji_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text("😀", fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Text Chat Button
                Box(
                    modifier = Modifier
                        .size(width = 72.dp, height = 42.dp)
                        .shadow(8.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFFFFB300))
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFD54F), Color(0xFFFFB300))
                            )
                        )
                        .border(1.dp, Color(0xFFFFF9C4), RoundedCornerShape(22.dp))
                        .clickable { showTextChatSheet = true }
                        .testTag("bottom_chat_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💬", fontSize = 20.sp)
                }
            }
        }

        // Floating Toast Feedback banner
        if (toastMessage != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .background(Color(0xFF00E676), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = toastMessage ?: "",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }

    // ==========================================
    // DIALOGS & POPUPS
    // ==========================================

    // 1. Settings Dialog
    if (showSettingsDialog) {
        InGameSettingsDialog(
            soundEnabled = soundEnabled,
            onToggleSound = onToggleSound,
            onSubmitFeedback = onSubmitFeedback,
            onLeaveMatch = {
                showSettingsDialog = false
                showExitDialog = true
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // 2. Leaderboard Dialog
    if (showLeaderboardDialog) {
        InGameLeaderboardDialog(
            gameState = gameState,
            onDismiss = { showLeaderboardDialog = false }
        )
    }

    // 3. Shop Dialog
    if (showShopDialog) {
        InGameShopDialog(
            coinBalance = coinBalance,
            gemBalance = gemBalance,
            onDismiss = { showShopDialog = false }
        )
    }

    // 4. Send Gift Dialog
    if (selectedGiftPlayer != null) {
        SendGiftDialog(
            targetPlayer = selectedGiftPlayer!!,
            coinBalance = coinBalance,
            onSend = { giftName, giftEmoji, cost ->
                val success = onSendGift(selectedGiftPlayer!!.id, giftName, giftEmoji, cost)
                if (success) {
                    toastMessage = "Sent $giftName $giftEmoji to ${selectedGiftPlayer!!.name}!"
                } else {
                    toastMessage = "Not enough coins to send $giftName!"
                }
                selectedGiftPlayer = null
            },
            onDismiss = { selectedGiftPlayer = null }
        )
    }

    // 5. Mute Specific Players Dialog
    if (showMutePlayersDialog) {
        MutePlayersManagementDialog(
            players = gameState.players,
            localUserId = localUserId,
            onTogglePlayerMic = onTogglePlayerMic,
            onDismiss = { showMutePlayersDialog = false }
        )
    }

    // 6. Emoji Reaction Sheet
    if (showEmojiSheet) {
        EmojiPickerModal(
            onSelectEmoji = { emoji ->
                if (localPlayer != null) {
                    onSendChatMessage(localPlayer.id, null, emoji)
                }
                showEmojiSheet = false
            },
            onDismiss = { showEmojiSheet = false }
        )
    }

    // 7. Text Chat Sheet
    if (showTextChatSheet) {
        QuickChatModal(
            onSendMessage = { text ->
                if (localPlayer != null) {
                    onSendChatMessage(localPlayer.id, text, null)
                }
                showTextChatSheet = false
            },
            onDismiss = { showTextChatSheet = false }
        )
    }

    // 8. Exit Confirmation Dialog
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
            containerColor = Color(0xFF1E1430),
            shape = RoundedCornerShape(18.dp)
        )
    }

    // 9. Match Over Victory Dialog
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
                        fontSize = 15.sp,
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
            containerColor = Color(0xFF1E1430),
            shape = RoundedCornerShape(22.dp)
        )
    }
}

// ==========================================
// SUB-COMPONENTS
// ==========================================

/**
 * Player Profile Card matching the screenshot:
 * Circular Avatar, Gift Icon badge, Microphone Icon (individual mute toggle), and Username.
 */
@Composable
private fun PlayerProfileSection(
    player: LudoPlayer,
    isActive: Boolean,
    isTop: Boolean,
    isAlignLeft: Boolean,
    onGiftClick: () -> Unit,
    onMicToggle: () -> Unit
) {
    val isMuted = player.controlState.micStatus == PlayerMicStatus.MUTED
    val activeEmoji = player.controlState.activeEmoji
    val chatMessage = player.controlState.lastChatMessage

    Column(
        horizontalAlignment = if (isAlignLeft) Alignment.Start else Alignment.End,
        modifier = Modifier.widthIn(max = 140.dp)
    ) {
        // If bottom, username is displayed above avatar
        if (!isTop) {
            Text(
                text = player.name,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // If align right, speech bubble or chat is on left of avatar
            if (!isAlignLeft && (activeEmoji != null || chatMessage != null)) {
                InGameChatBubble(emoji = activeEmoji, text = chatMessage, pointRight = true)
            }

            // Mic on the left for left-aligned players
            if (isAlignLeft) {
                MicrophoneIndicatorButton(isMuted = isMuted, onClick = onMicToggle)
            }

            // Avatar Container with Gift Badge
            Box(
                modifier = Modifier
                    .size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glowing border if active turn
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .shadow(if (isActive) 10.dp else 4.dp, CircleShape, spotColor = if (isActive) Color(0xFF00E676) else Color.Transparent)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = when (player.color) {
                                    PlayerColor.YELLOW -> listOf(Color(0xFFFFD54F), Color(0xFFF57F17))
                                    PlayerColor.BLUE -> listOf(Color(0xFF42A5F5), Color(0xFF1565C0))
                                    PlayerColor.GREEN -> listOf(Color(0xFF66BB6A), Color(0xFF2E7D32))
                                    PlayerColor.RED -> listOf(Color(0xFFEF5350), Color(0xFFC62828))
                                }
                            )
                        )
                        .border(if (isActive) 2.5.dp else 1.5.dp, if (isActive) Color(0xFF00E676) else Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (player.color) {
                            PlayerColor.YELLOW -> "🦁"
                            PlayerColor.BLUE -> "🦉"
                            PlayerColor.GREEN -> "🐼"
                            PlayerColor.RED -> "👑"
                        },
                        fontSize = 24.sp
                    )
                }

                // Gift Icon Badge on Top-Right of Avatar
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                            )
                        )
                        .border(1.dp, Color.White, CircleShape)
                        .clickable { onGiftClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎁", fontSize = 11.sp)
                }
            }

            // Mic on the right for right-aligned players
            if (!isAlignLeft) {
                MicrophoneIndicatorButton(isMuted = isMuted, onClick = onMicToggle)
            }

            // If align left, speech bubble or chat is on right of avatar
            if (isAlignLeft && (activeEmoji != null || chatMessage != null)) {
                InGameChatBubble(emoji = activeEmoji, text = chatMessage, pointRight = false)
            }
        }

        // If top, username is displayed below avatar
        if (isTop) {
            Text(
                text = player.name,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * Microphone Icon with volume animation bars, functioning as an individual mute toggle
 */
@Composable
private fun MicrophoneIndicatorButton(
    isMuted: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        // Sound wave volume bars
        if (!isMuted) {
            Column(
                verticalArrangement = Arrangement.spacedBy(1.5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.width(3.dp).height(8.dp).background(Color(0xFF00E676), RoundedCornerShape(1.dp)))
                Box(modifier = Modifier.width(3.dp).height(12.dp).background(Color(0xFF00E676), RoundedCornerShape(1.dp)))
                Box(modifier = Modifier.width(3.dp).height(6.dp).background(Color(0xFF00E676), RoundedCornerShape(1.dp)))
            }
        }

        // Mic Button
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isMuted) Color(0xFFD32F2F) else Color(0xFF2E7D32))
                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Mute Toggle",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * In-Game Speech / Reaction Bubble
 */
@Composable
private fun InGameChatBubble(
    emoji: String?,
    text: String?,
    pointRight: Boolean
) {
    Box(
        modifier = Modifier
            .shadow(6.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF3B2D54))
            .border(1.dp, Color(0xFF7E57C2), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (emoji != null) {
            Text(text = emoji, fontSize = 22.sp)
        } else if (text != null) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
        }
    }
}

// ==========================================
// MODAL DIALOGS
// ==========================================

/**
 * 1. Settings Dialog: Toggle Sound, Submit Feedback, Leave Match
 */
@Composable
private fun InGameSettingsDialog(
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onSubmitFeedback: (String) -> Unit,
    onLeaveMatch: () -> Unit,
    onDismiss: () -> Unit
) {
    var feedbackText by remember { mutableStateOf("") }
    var feedbackSubmitted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚙️ Game Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Sound Effects Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sound Effects", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { onToggleSound() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E676))
                    )
                }

                Divider(color = Color(0xFF3D2B5A), thickness = 1.dp)

                // Feedback Section
                Text("Submit Feedback", color = Color(0xFFFFD54F), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (feedbackSubmitted) {
                    Text(
                        "Thank you for your feedback! Our team will review it.",
                        color = Color(0xFF00E676),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        placeholder = { Text("How can we improve the match?", color = Color(0xFF90A4AE), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF5E4588),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Button(
                        onClick = {
                            if (feedbackText.isNotBlank()) {
                                onSubmitFeedback(feedbackText)
                                feedbackSubmitted = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send Feedback", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = Color(0xFF3D2B5A), thickness = 1.dp)

                // Forfeit Match Button
                Button(
                    onClick = onLeaveMatch,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Forfeit & Leave Match", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {},
        containerColor = Color(0xFF1E1430),
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * 2. Match Leaderboard / Scoreboard
 */
@Composable
private fun InGameLeaderboardDialog(
    gameState: LudoGameState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🏆 Match Standings", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${gameState.mode.title} • Prize: ${gameState.mode.prizePool} 🪙",
                    color = Color(0xFF00E676),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                gameState.players.forEachIndexed { index, player ->
                    val finishedCount = player.pawns.count { it.isFinished }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF281C3E), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF5E4588), RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("#${index + 1}", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(player.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Text("$finishedCount/4 Home", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = Color(0xFF180F26),
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * 3. Shop & Token Skins Preview
 */
@Composable
private fun InGameShopDialog(
    coinBalance: Long,
    gemBalance: Long,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🛍️ In-Game Skins", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🪙 Coins: $coinBalance", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("💎 Gems: $gemBalance", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Divider(color = Color(0xFF3D2B5A), thickness = 1.dp)

                val skins = listOf(
                    Triple("Classic Glossy", "♟️", "Equipped"),
                    Triple("Golden Crown", "👑", "1,000 🪙"),
                    Triple("Soda Can 3D", "🥤", "2,500 🪙"),
                    Triple("Diamond Star", "⭐", "50 💎")
                )

                skins.forEach { (name, icon, status) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF281C3E), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(icon, fontSize = 20.sp)
                            Text(name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Button(
                            onClick = { onDismiss() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (status == "Equipped") Color(0xFF43A047) else Color(0xFF7E57C2)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(status, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = Color(0xFF180F26),
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * 4. Send Gift Dialog
 */
@Composable
private fun SendGiftDialog(
    targetPlayer: LudoPlayer,
    coinBalance: Long,
    onSend: (giftName: String, giftEmoji: String, cost: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val gifts = listOf(
        Triple("Rose", "🌹", 50L),
        Triple("Coffee", "☕", 100L),
        Triple("Pizza", "🍕", 200L),
        Triple("Trophy", "🏆", 500L),
        Triple("Diamond", "💎", 1000L)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎁 Gift ${targetPlayer.name}", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Balance: $coinBalance 🪙", color = Color(0xFF00E676), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                gifts.forEach { (name, emoji, cost) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF281C3E), RoundedCornerShape(10.dp))
                            .clickable { onSend(name, emoji, cost) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(emoji, fontSize = 24.sp)
                            Text(name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Text("$cost 🪙", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = Color(0xFF180F26),
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * 5. Mute Specific Players Dialog
 */
@Composable
private fun MutePlayersManagementDialog(
    players: List<LudoPlayer>,
    localUserId: String,
    onTogglePlayerMic: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎙️ Player Voice Mutes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                players.forEach { p ->
                    val isMuted = p.controlState.micStatus == PlayerMicStatus.MUTED
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF281C3E), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = p.name + if (p.id == localUserId) " (You)" else "",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Button(
                            onClick = { onTogglePlayerMic(p.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMuted) Color(0xFFD32F2F) else Color(0xFF388E3C)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(if (isMuted) "Muted" else "Hearing", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = Color(0xFF180F26),
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * 6. Emoji Picker Modal
 */
@Composable
private fun EmojiPickerModal(
    onSelectEmoji: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val emojis = listOf("😀", "🔥", "👑", "😂", "😎", "🎯", "🚀", "💎", "👍", "❤️", "😡", "🎉")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Send Reaction", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(emojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF281C3E))
                            .clickable { onSelectEmoji(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 26.sp)
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = Color(0xFF180F26),
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * 7. Quick Chat Modal
 */
@Composable
private fun QuickChatModal(
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var customText by remember { mutableStateOf("") }
    val quickPhrases = listOf(
        "Good luck! 🍀",
        "Well played! 👏",
        "Hurry up please! ⏳",
        "Rolling a six! 🎲",
        "Nice move! 🔥",
        "Oops! 😅",
        "Rematch? ⚔️",
        "Thanks! 🙏"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("In-Game Chat", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Preset phrases
                quickPhrases.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { phrase ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF281C3E))
                                    .clickable { onSendMessage(phrase) }
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(phrase, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Custom message input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it },
                        placeholder = { Text("Say something...", color = Color(0xFF90A4AE), fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Button(
                        onClick = {
                            if (customText.isNotBlank()) {
                                onSendMessage(customText)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                    ) {
                        Text("Send", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = Color(0xFF180F26),
        shape = RoundedCornerShape(20.dp)
    )
}
