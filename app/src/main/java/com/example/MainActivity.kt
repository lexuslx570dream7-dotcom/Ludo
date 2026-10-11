package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.GameMode
import com.example.model.LudoPlayer
import com.example.model.PlayerColor
import com.example.ui.components.BottomNavBar
import com.example.ui.components.CoinGemDialog
import com.example.ui.components.TopStatusBar
import com.example.ui.screens.*
import com.example.ui.theme.GameBgDeep
import com.example.ui.theme.LudoGameTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.LudoViewModel
import com.example.viewmodel.StoreType
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    private val viewModel: LudoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LudoGameTheme {
                AppRootContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppRootContent(viewModel: LudoViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            auth.currentUser?.let { user ->
                viewModel.onUserAuthenticated(user)
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        SignInScreen(
            onSignInSuccess = {
                val user = Firebase.auth.currentUser
                currentUser = user
                user?.let { viewModel.onUserAuthenticated(it) }
            }
        )
    } else {
        MainAppContent(
            viewModel = viewModel,
            onSignOut = {
                signOutUser(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {
                        currentUser = null
                    },
                    scope = coroutineScope
                )
            }
        )
    }
}

@Composable
fun MainAppContent(
    viewModel: LudoViewModel,
    onSignOut: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userAccount by viewModel.repository.userAccount.collectAsStateWithLifecycle()
    val storeType by viewModel.selectedStoreType.collectAsStateWithLifecycle()
    val friends by viewModel.repository.friends.collectAsStateWithLifecycle()
    val friendRequests by viewModel.repository.friendRequests.collectAsStateWithLifecycle()
    val selectedFriend by viewModel.selectedFriendChat.collectAsStateWithLifecycle()
    val allMessages by viewModel.repository.messages.collectAsStateWithLifecycle()
    val tournaments by viewModel.repository.tournaments.collectAsStateWithLifecycle()
    val achievements by viewModel.repository.achievements.collectAsStateWithLifecycle()
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsStateWithLifecycle()
    val recordingDuration by viewModel.recordingDuration.collectAsStateWithLifecycle()
    val matchedPlayers by viewModel.matchedPlayers.collectAsStateWithLifecycle()
    val matchmakingProgress by viewModel.matchmakingProgress.collectAsStateWithLifecycle()
    val matchmakingMode by viewModel.matchmakingMode.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val privateRoomCode by viewModel.privateRoomCode.collectAsStateWithLifecycle()
    val privateRoomPlayers by viewModel.privateRoomPlayers.collectAsStateWithLifecycle()
    val isRoomOwner by viewModel.isRoomOwner.collectAsStateWithLifecycle()

    val currentFriendMessages = remember(selectedFriend, allMessages) {
        if (selectedFriend != null) allMessages[selectedFriend?.id] ?: emptyList() else emptyList()
    }

    // Handle back button for secondary screens
    if (currentScreen != AppScreen.HOME) {
        BackHandler {
            when (currentScreen) {
                AppScreen.GAME -> viewModel.leaveGame()
                AppScreen.MATCHMAKING -> viewModel.cancelMatchmaking()
                else -> viewModel.navigateTo(AppScreen.HOME)
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(GameBgDeep),
        containerColor = GameBgDeep,
        topBar = {
            // Realistic Android Status Bar matching screenshot
            TopStatusBar()
        },
        bottomBar = {
            // Show bottom navigation bar on primary tabs
            if (currentScreen in listOf(AppScreen.HOME, AppScreen.SOCIAL, AppScreen.PROFILE)) {
                BottomNavBar(
                    currentScreen = currentScreen,
                    onTabSelected = { tab -> viewModel.navigateTo(tab) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        userAccount = userAccount,
                        onAvatarClick = { viewModel.navigateTo(AppScreen.PROFILE) },
                        onAddCoinsClick = { viewModel.openStore(StoreType.COINS) },
                        onAddGemsClick = { viewModel.openStore(StoreType.GEMS) },
                        onSelectMode = { mode -> viewModel.startMatchmaking(mode) },
                        onOpenTournament = { viewModel.navigateTo(AppScreen.TOURNAMENTS) },
                        onOpenPrivateLocal = { viewModel.navigateTo(AppScreen.PRIVATE_ROOM) },
                        onOpenVipRoom = { viewModel.navigateTo(AppScreen.VIP_ROOM) }
                    )
                }

                AppScreen.SOCIAL -> {
                    SocialScreen(
                        friends = friends,
                        friendRequests = friendRequests,
                        selectedFriend = selectedFriend,
                        chatMessages = currentFriendMessages,
                        isRecordingVoice = isRecordingVoice,
                        recordingDuration = recordingDuration,
                        onSelectFriend = { friend -> viewModel.openFriendChat(friend) },
                        onCloseChat = { viewModel.closeFriendChat() },
                        onSendMessage = { text ->
                            selectedFriend?.let { f ->
                                viewModel.repository.sendMessage(f.id, text = text)
                            }
                        },
                        onStartVoiceRecord = { viewModel.startVoiceRecording() },
                        onStopVoiceRecord = { friendId -> viewModel.stopAndSendVoiceRecording(friendId) },
                        onSendImage = { friendId ->
                            viewModel.repository.sendMessage(friendId, text = null, imageUrl = "sample_img")
                        },
                        onAcceptRequest = { reqId -> viewModel.repository.acceptFriendRequest(reqId) },
                        onRejectRequest = { reqId -> viewModel.repository.rejectFriendRequest(reqId) },
                        onAddFriend = { name -> viewModel.repository.addFriend(name) },
                        onRemoveFriend = { id -> viewModel.repository.removeFriend(id) }
                    )
                }

                AppScreen.PROFILE -> {
                    ProfileScreen(
                        userAccount = userAccount,
                        achievements = achievements,
                        onToggleMusic = { on -> viewModel.repository.updateSettings(music = on) },
                        onToggleSound = { on -> viewModel.repository.updateSettings(sound = on) },
                        onToggleVibration = { on -> viewModel.repository.updateSettings(vibration = on) },
                        onUpdateUsername = { name ->
                            viewModel.repository.updateSettings(username = name)
                            viewModel.persistUserProfile()
                        },
                        onAddCoinsClick = { viewModel.openStore(StoreType.COINS) },
                        onAddGemsClick = { viewModel.openStore(StoreType.GEMS) },
                        onSignOut = onSignOut
                    )
                }

                AppScreen.MATCHMAKING -> {
                    MatchmakingScreen(
                        mode = matchmakingMode,
                        matchedPlayers = matchedPlayers,
                        progress = matchmakingProgress,
                        onCancel = { viewModel.cancelMatchmaking() }
                    )
                }

                AppScreen.GAME -> {
                    GameScreen(
                        gameState = gameState,
                        localUserId = viewModel.repository.userAccount.value.userId,
                        onRollDice = { viewModel.rollDice() },
                        onPawnClick = { pawnId -> viewModel.movePawn(pawnId) },
                        onActivateMagic = { playerId -> viewModel.activatePlayerMagic(playerId) },
                        onToggleChat = { playerId -> viewModel.togglePlayerChat(playerId) },
                        onSendChatMessage = { playerId, text, emoji -> viewModel.sendPlayerChatMessage(playerId, text, emoji) },
                        onTogglePlayerMic = { playerId -> viewModel.togglePlayerMic(playerId) },
                        onToggleMic = { viewModel.toggleInGameMic() },
                        onLeaveGame = { viewModel.leaveGame() }
                    )
                }

                AppScreen.TOURNAMENTS -> {
                    TournamentScreen(
                        tournaments = tournaments,
                        onJoinTournament = { t ->
                            if (viewModel.repository.spendCoins(t.entryFeeCoins)) {
                                viewModel.persistUserProfile()
                                viewModel.initializeGame(
                                    GameMode.TOURNAMENT,
                                    listOf(
                                        LudoPlayer(id = userAccount.userId, name = userAccount.username, color = PlayerColor.RED),
                                        LudoPlayer(id = "tourn1", name = "Titan99", color = PlayerColor.GREEN, isBot = true),
                                        LudoPlayer(id = "tourn2", name = "Blaze", color = PlayerColor.YELLOW, isBot = true),
                                        LudoPlayer(id = "tourn3", name = "CyberLudo", color = PlayerColor.BLUE, isBot = true)
                                    )
                                )
                            } else {
                                viewModel.openStore(StoreType.COINS)
                            }
                        },
                        onCreateTournament = { name, entry, prize, max ->
                            viewModel.repository.createTournament(name, entry, prize, max)
                        },
                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                    )
                }

                AppScreen.PRIVATE_ROOM -> {
                    PrivateRoomScreen(
                        roomCode = privateRoomCode,
                        players = privateRoomPlayers,
                        isOwner = isRoomOwner,
                        onGenerateNewCode = { viewModel.generateNewPrivateRoomCode() },
                        onAddBotPlayer = { viewModel.addBotToPrivateRoom() },
                        onStartGame = { viewModel.startPrivateRoomGame() },
                        onStartLocalMatch = { count ->
                            val localPlayers = (0 until count).map { idx ->
                                val color = PlayerColor.values()[idx]
                                LudoPlayer(
                                    id = "local_$idx",
                                    name = "Player ${idx + 1} (${color.displayName})",
                                    color = color,
                                    isBot = false
                                )
                            }
                            viewModel.initializeGame(GameMode.LOCAL_PASS_AND_PLAY, localPlayers)
                        },
                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                    )
                }

                AppScreen.VIP_ROOM -> {
                    VIPRoomScreen(
                        userCoins = userAccount.coins,
                        onEnterVipMatch = { mode -> viewModel.startMatchmaking(mode) },
                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                    )
                }
            }

            // Coin & Gem Store / Reward Claim Dialog
            if (storeType != StoreType.NONE) {
                CoinGemDialog(
                    storeType = storeType,
                    onClaimFreeReward = {
                        if (storeType == StoreType.COINS) {
                            viewModel.repository.addCoins(500L)
                        } else {
                            viewModel.repository.addGems(2L)
                        }
                        viewModel.persistUserProfile()
                        viewModel.closeStore()
                    },
                    onPurchasePack = { amount, isCoin ->
                        if (isCoin) {
                            viewModel.repository.addCoins(amount)
                        } else {
                            viewModel.repository.addGems(amount)
                        }
                        viewModel.persistUserProfile()
                        viewModel.closeStore()
                    },
                    onDismiss = { viewModel.closeStore() }
                )
            }
        }
    }
}
