package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.FirestoreGameRoom
import com.example.data.FirestoreLudoRepository
import com.example.data.FirestoreUserProfile
import com.example.data.LudoRepository
import com.example.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppScreen {
    HOME,
    SOCIAL,
    PROFILE,
    MATCHMAKING,
    GAME,
    TOURNAMENTS,
    PRIVATE_ROOM,
    VIP_ROOM
}

enum class StoreType {
    NONE,
    COINS,
    GEMS
}

class LudoViewModel(application: Application) : AndroidViewModel(application) {

    val repository = LudoRepository()
    val firestoreRepository = FirestoreLudoRepository(application)
    val soundManager = SoundManager(application)

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedStoreType = MutableStateFlow(StoreType.NONE)
    val selectedStoreType: StateFlow<StoreType> = _selectedStoreType.asStateFlow()

    private val _selectedFriendChat = MutableStateFlow<Friend?>(null)
    val selectedFriendChat: StateFlow<Friend?> = _selectedFriendChat.asStateFlow()

    // Matchmaking state
    private val _matchmakingMode = MutableStateFlow(GameMode.FOUR_PLAYERS)
    val matchmakingMode: StateFlow<GameMode> = _matchmakingMode.asStateFlow()

    private val _matchmakingProgress = MutableStateFlow(0f)
    val matchmakingProgress: StateFlow<Float> = _matchmakingProgress.asStateFlow()

    private val _matchedPlayers = MutableStateFlow<List<LudoPlayer>>(emptyList())
    val matchedPlayers: StateFlow<List<LudoPlayer>> = _matchedPlayers.asStateFlow()

    // Game state
    private val _gameState = MutableStateFlow(LudoGameState())
    val gameState: StateFlow<LudoGameState> = _gameState.asStateFlow()

    // Private Room State
    private val _privateRoomCode = MutableStateFlow("LD-" + Random.nextInt(100000, 999999).toString())
    val privateRoomCode: StateFlow<String> = _privateRoomCode.asStateFlow()

    private val _privateRoomPlayers = MutableStateFlow<List<LudoPlayer>>(emptyList())
    val privateRoomPlayers: StateFlow<List<LudoPlayer>> = _privateRoomPlayers.asStateFlow()

    private val _isRoomOwner = MutableStateFlow(true)
    val isRoomOwner: StateFlow<Boolean> = _isRoomOwner.asStateFlow()

    // Voice message recording simulation state
    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0)
    val recordingDuration: StateFlow<Int> = _recordingDuration.asStateFlow()

    private var botTurnJob: Job? = null
    private var turnTimerJob: Job? = null
    private var recordingJob: Job? = null
    private val magicCooldownJobs = mutableMapOf<String, Job>()
    private val chatBubbleJobs = mutableMapOf<String, Job>()

    init {
        // Initialize private room with the host
        val host = LudoPlayer(
            id = repository.userAccount.value.userId,
            name = repository.userAccount.value.username,
            color = PlayerColor.RED
        )
        _privateRoomPlayers.value = listOf(host)

        // Check if user is already signed in
        Firebase.auth.currentUser?.let { onUserAuthenticated(it) }
    }

    fun onUserAuthenticated(user: FirebaseUser) {
        viewModelScope.launch {
            val uid = user.uid
            val existing = firestoreRepository.getUserProfile(uid)
            if (existing != null) {
                repository.syncFromFirestore(
                    userId = uid,
                    username = existing.username,
                    level = existing.level,
                    coins = existing.coins,
                    gems = existing.gems,
                    totalGames = existing.totalGames,
                    wins = existing.wins,
                    losses = existing.losses,
                    winStreak = existing.winStreak
                )
            } else {
                val displayName = user.displayName ?: "Player_${uid.take(5)}"
                val newProfile = FirestoreUserProfile(
                    userId = uid,
                    username = displayName,
                    level = 12,
                    coins = 2000L,
                    gems = 4L
                )
                firestoreRepository.saveUserProfile(newProfile)
                repository.syncFromFirestore(
                    userId = uid,
                    username = displayName,
                    level = 12,
                    coins = 2000L,
                    gems = 4L,
                    totalGames = 0,
                    wins = 0,
                    losses = 0,
                    winStreak = 0
                )
            }
        }
    }

    fun persistUserProfile() {
        viewModelScope.launch {
            val currentUser = Firebase.auth.currentUser ?: return@launch
            val account = repository.userAccount.value
            val profile = FirestoreUserProfile(
                userId = currentUser.uid,
                username = account.username,
                level = account.level,
                coins = account.coins,
                gems = account.gems,
                totalGames = account.totalGames,
                wins = account.wins,
                losses = account.losses,
                winStreak = account.winStreak
            )
            firestoreRepository.saveUserProfile(profile)
        }
    }

    private fun syncRoomToFirestore(matchId: String, mode: GameMode, players: List<LudoPlayer>) {
        viewModelScope.launch {
            val currentUser = Firebase.auth.currentUser ?: return@launch
            val room = FirestoreGameRoom(
                id = matchId,
                mode = mode.name,
                status = "PLAYING",
                creatorId = currentUser.uid,
                playerUids = players.map { it.id }
            )
            firestoreRepository.createGameRoom(room)
        }
    }

    private fun syncRoomTurn(state: LudoGameState) {
        viewModelScope.launch {
            val room = FirestoreGameRoom(
                id = state.matchId,
                mode = state.mode.name,
                status = if (state.isGameOver) "COMPLETED" else "PLAYING",
                creatorId = Firebase.auth.currentUser?.uid ?: "",
                playerUids = state.players.map { it.id },
                currentTurnUid = state.currentPlayer?.id ?: "",
                diceValue = state.diceValue ?: 1,
                diceRolled = state.diceRolled,
                winnerUid = state.winner?.id ?: ""
            )
            firestoreRepository.updateGameRoomState(room)
        }
    }

    fun navigateTo(screen: AppScreen) {
        soundManager.playClick(repository.userAccount.value.soundEnabled)
        _currentScreen.value = screen
    }

    fun openStore(storeType: StoreType) {
        soundManager.playClick(repository.userAccount.value.soundEnabled)
        _selectedStoreType.value = storeType
    }

    fun closeStore() {
        _selectedStoreType.value = StoreType.NONE
    }

    fun openFriendChat(friend: Friend) {
        soundManager.playClick(repository.userAccount.value.soundEnabled)
        _selectedFriendChat.value = friend
    }

    fun closeFriendChat() {
        _selectedFriendChat.value = null
    }

    // Matchmaking Flow
    fun startMatchmaking(mode: GameMode) {
        soundManager.playClick(repository.userAccount.value.soundEnabled)
        if (!repository.spendCoins(mode.entryFee)) {
            // Not enough coins, prompt coin store
            _selectedStoreType.value = StoreType.COINS
            return
        }

        _matchmakingMode.value = mode
        _matchmakingProgress.value = 0f
        _currentScreen.value = AppScreen.MATCHMAKING

        val myAccount = repository.userAccount.value
        val me = LudoPlayer(
            id = myAccount.userId,
            name = myAccount.username,
            color = PlayerColor.RED,
            level = myAccount.level
        )

        val potentialOpponents = listOf(
            LudoPlayer(id = "opp1", name = "Zara Star", color = PlayerColor.GREEN, isBot = true, level = 10),
            LudoPlayer(id = "opp2", name = "Vikram99", color = PlayerColor.YELLOW, isBot = true, level = 14),
            LudoPlayer(id = "opp3", name = "Leo King", color = PlayerColor.BLUE, isBot = true, level = 12)
        )

        val requiredCount = when (mode) {
            GameMode.TWO_PLAYERS -> 2
            else -> 4
        }

        viewModelScope.launch {
            _matchedPlayers.value = listOf(me)
            for (step in 1..requiredCount) {
                delay(700)
                _matchmakingProgress.value = step.toFloat() / requiredCount
                if (step > 1 && step - 2 < potentialOpponents.size) {
                    _matchedPlayers.value = _matchedPlayers.value + potentialOpponents[step - 2]
                }
            }
            delay(500)
            // Match found! Launch game
            initializeGame(mode, _matchedPlayers.value)
        }
    }

    fun cancelMatchmaking() {
        repository.addCoins(_matchmakingMode.value.entryFee) // Refund
        _currentScreen.value = AppScreen.HOME
    }

    // Game Session Logic
    fun initializeGame(mode: GameMode, rawPlayers: List<LudoPlayer>) {
        val matchId = "MATCH-${Random.nextInt(1000, 9999)}"
        val myUserId = repository.userAccount.value.userId
        val initializedPlayers = rawPlayers.mapIndexed { index, player ->
            player.copy(
                controlState = PlayerControlState(
                    playerId = player.id,
                    magicButtonStatus = MagicButtonStatus.READY,
                    magicCooldownSeconds = 0,
                    isMagicEffectActive = false,
                    isChatOpen = false,
                    lastChatMessage = null,
                    activeEmoji = null,
                    micStatus = PlayerMicStatus.MUTED,
                    turnStatus = if (index == 0) PlayerTurnStatus.ACTIVE else PlayerTurnStatus.WAITING,
                    turnTimeRemaining = 15,
                    connectionStatus = PlayerConnectionStatus.ONLINE
                )
            )
        }
        _gameState.value = LudoGameState(
            matchId = matchId,
            mode = mode,
            players = initializedPlayers,
            currentTurnIndex = 0,
            diceValue = null,
            isRolling = false,
            canRoll = true,
            selectablePawnIds = emptyList(),
            consecutiveSixes = 0,
            logMessage = "${initializedPlayers.firstOrNull()?.name ?: "Player"}'s turn to roll!",
            turnTimeRemaining = 15,
            micEnabled = false
        )
        _currentScreen.value = AppScreen.GAME
        syncRoomToFirestore(matchId, mode, initializedPlayers)
        startTurnTimer()
    }

    private fun startTurnTimer() {
        turnTimerJob?.cancel()
        val activePlayer = _gameState.value.currentPlayer ?: return
        val activeId = activePlayer.id

        // Mark active player as ACTIVE with 15s remaining, and others as WAITING
        _gameState.update { state ->
            val updated = state.players.map { p ->
                if (p.id == activeId) {
                    p.copy(controlState = p.controlState.copy(turnStatus = PlayerTurnStatus.ACTIVE, turnTimeRemaining = 15))
                } else {
                    p.copy(controlState = p.controlState.copy(turnStatus = PlayerTurnStatus.WAITING, turnTimeRemaining = 15))
                }
            }
            state.copy(players = updated, turnTimeRemaining = 15)
        }

        turnTimerJob = viewModelScope.launch {
            for (sec in 15 downTo 0) {
                _gameState.update { state ->
                    val updated = state.players.map { p ->
                        if (p.id == activeId) {
                            p.copy(controlState = p.controlState.copy(turnTimeRemaining = sec))
                        } else {
                            p
                        }
                    }
                    state.copy(players = updated, turnTimeRemaining = sec)
                }
                delay(1000)
            }

            // Timer reached 0: Automatic turn passing!
            val currentState = _gameState.value
            val timedOutPlayer = currentState.currentPlayer
            if (timedOutPlayer != null && timedOutPlayer.id == activeId && !currentState.isGameOver) {
                _gameState.update { state ->
                    val updated = state.players.map { p ->
                        if (p.id == activeId) {
                            p.copy(controlState = p.controlState.copy(turnStatus = PlayerTurnStatus.TIMEOUT, turnTimeRemaining = 0))
                        } else {
                            p
                        }
                    }
                    state.copy(
                        players = updated,
                        canRoll = false,
                        selectablePawnIds = emptyList(),
                        logMessage = "⏰ ${timedOutPlayer.name}'s turn timed out! Turn passed."
                    )
                }
                soundManager.playTurnAlert(repository.userAccount.value.soundEnabled)
                delay(800)
                nextTurn()
            }
        }
    }

    fun rollDice(requestingPlayerId: String? = null) {
        val state = _gameState.value
        val activePlayer = state.currentPlayer ?: return

        // Verify turn authorization: only current player can roll
        if (requestingPlayerId != null && requestingPlayerId != activePlayer.id) return
        if (!state.canRoll || state.isRolling || state.isGameOver) return
        if (activePlayer.controlState.turnStatus == PlayerTurnStatus.TIMEOUT) return

        // Stop timer immediately once player initiates turn roll
        turnTimerJob?.cancel()

        val userPrefs = repository.userAccount.value
        soundManager.playDiceRoll(userPrefs.soundEnabled, userPrefs.vibrationEnabled)

        viewModelScope.launch {
            _gameState.update { it.copy(isRolling = true, canRoll = false) }

            // Dice roll animation ticks
            for (i in 1..6) {
                _gameState.update { it.copy(diceValue = Random.nextInt(1, 7)) }
                delay(60)
            }

            // Check if active player had Magic effect active
            val hadMagic = activePlayer.controlState.isMagicEffectActive
            val finalDice = if (hadMagic) {
                6 // Magic guarantee
            } else {
                Random.nextInt(1, 7)
            }

            // Consume magic effect and start independent cooldown for activePlayer only
            if (hadMagic) {
                _gameState.update { s ->
                    s.updatePlayerControl(activePlayer.id) { ctrl ->
                        ctrl.copy(
                            isMagicEffectActive = false,
                            magicButtonStatus = MagicButtonStatus.COOLDOWN,
                            magicCooldownSeconds = 30,
                            magicAnimationTag = null
                        )
                    }
                }
                launchPlayerMagicCooldown(activePlayer.id)
            }

            val newSixes = if (finalDice == 6) state.consecutiveSixes + 1 else 0

            // If 3 sixes in a row, forfeit turn
            if (newSixes >= 3) {
                _gameState.update {
                    it.copy(
                        diceValue = finalDice,
                        isRolling = false,
                        consecutiveSixes = 0,
                        logMessage = "${activePlayer.name} rolled three 6s! Turn forfeited."
                    )
                }
                delay(900)
                nextTurn()
                return@launch
            }

            val movablePawnIds = LudoLogic.getMovablePawnIds(activePlayer, finalDice)

            _gameState.update {
                it.copy(
                    diceValue = finalDice,
                    isRolling = false,
                    consecutiveSixes = newSixes,
                    selectablePawnIds = movablePawnIds,
                    logMessage = if (hadMagic) "✨ ${activePlayer.name} used MAGIC and rolled a $finalDice!" else "${activePlayer.name} rolled a $finalDice!"
                )
            }

            if (movablePawnIds.isEmpty()) {
                // No valid moves -> automatic turn pass
                delay(900)
                nextTurn()
            } else if (movablePawnIds.size == 1) {
                // Only one movable pawn -> move automatically
                delay(400)
                movePawn(movablePawnIds.first(), activePlayer.id)
            } else {
                // Multiple choices -> resume turn timer for choice
                startTurnTimer()
                if (activePlayer.isBot) {
                    delay(500)
                    movePawn(movablePawnIds.random(), activePlayer.id)
                }
            }
        }
    }

    fun movePawn(pawnId: Int, requestingPlayerId: String? = null) {
        val state = _gameState.value
        val activePlayer = state.currentPlayer ?: return
        if (requestingPlayerId != null && requestingPlayerId != activePlayer.id) return
        val dice = state.diceValue ?: return
        if (!state.selectablePawnIds.contains(pawnId)) return

        // Stop timer immediately on pawn move
        turnTimerJob?.cancel()

        val userPrefs = repository.userAccount.value
        soundManager.playPawnMove(userPrefs.soundEnabled)

        val isTeam = state.mode == GameMode.TEAM_2V2
        val moveResult = LudoLogic.executeMove(
            players = state.players,
            activePlayer = activePlayer,
            pawnId = pawnId,
            diceRoll = dice,
            isTeamMode = isTeam
        )

        if (moveResult.capturedOpponent != null) {
            soundManager.playCapture(userPrefs.soundEnabled, userPrefs.vibrationEnabled)
        }

        // Check if active player won
        val updatedActive = moveResult.updatedPlayers.first { it.id == activePlayer.id }
        val playerWon = updatedActive.isWinner

        _gameState.update {
            it.copy(
                players = moveResult.updatedPlayers,
                selectablePawnIds = emptyList(),
                logMessage = moveResult.logMessage
            )
        }

        viewModelScope.launch {
            if (playerWon) {
                soundManager.playVictory(userPrefs.soundEnabled)
                val isMe = activePlayer.id == repository.userAccount.value.userId
                repository.addGameStats(
                    won = isMe,
                    coinsEarned = if (isMe) state.mode.prizePool else 50L
                )
                _gameState.update {
                    it.copy(
                        isGameOver = true,
                        logMessage = "👑 ${activePlayer.name} has WON the match!"
                    )
                }
                persistUserProfile()
                syncRoomTurn(_gameState.value)
            } else if (moveResult.awardsExtraTurn) {
                // Extra turn for active player!
                delay(600)
                _gameState.update {
                    it.copy(
                        canRoll = true,
                        diceValue = null,
                        logMessage = "${activePlayer.name} gets a BONUS roll! 🎲"
                    )
                }
                syncRoomTurn(_gameState.value)
                startTurnTimer()
                triggerBotTurnIfNeeded()
            } else {
                delay(600)
                nextTurn()
            }
        }
    }

    private fun nextTurn() {
        val state = _gameState.value
        if (state.isGameOver) return

        val nextIndex = (state.currentTurnIndex + 1) % state.players.size
        val nextPlayer = state.players[nextIndex]

        val updatedPlayers = state.players.mapIndexed { idx, p ->
            if (idx == nextIndex) {
                p.copy(controlState = p.controlState.copy(turnStatus = PlayerTurnStatus.ACTIVE, turnTimeRemaining = 15))
            } else {
                p.copy(controlState = p.controlState.copy(turnStatus = PlayerTurnStatus.WAITING, turnTimeRemaining = 15))
            }
        }

        _gameState.update {
            it.copy(
                players = updatedPlayers,
                currentTurnIndex = nextIndex,
                diceValue = null,
                canRoll = true,
                selectablePawnIds = emptyList(),
                consecutiveSixes = 0,
                logMessage = "${nextPlayer.name}'s turn to roll!",
                turnTimeRemaining = 15
            )
        }
        syncRoomTurn(_gameState.value)
        startTurnTimer()
        triggerBotTurnIfNeeded()
    }

    private fun triggerBotTurnIfNeeded() {
        botTurnJob?.cancel()
        val nextPlayer = _gameState.value.currentPlayer ?: return
        if (nextPlayer.isBot && !_gameState.value.isGameOver) {
            botTurnJob = viewModelScope.launch {
                delay(600)
                // If bot has magic ready and pawns stuck in base, bot activates its own magic
                val allInBase = nextPlayer.pawns.all { it.isInBase }
                if (allInBase && nextPlayer.controlState.magicButtonStatus == MagicButtonStatus.READY && Random.nextFloat() < 0.6f) {
                    activatePlayerMagic(nextPlayer.id)
                    delay(500)
                }
                rollDice(nextPlayer.id)
            }
        }
    }

    // Independent Player Magic Controls
    fun activatePlayerMagic(playerId: String) {
        val state = _gameState.value
        val player = state.getPlayer(playerId) ?: return

        // Authorization check
        val myUserId = repository.userAccount.value.userId
        if (player.id != myUserId && !player.isBot) return

        // State check
        if (player.controlState.magicButtonStatus != MagicButtonStatus.READY || player.controlState.isMagicEffectActive) {
            return
        }

        _gameState.update { s ->
            s.updatePlayerControl(playerId) { ctrl ->
                ctrl.copy(
                    magicButtonStatus = MagicButtonStatus.ACTIVE,
                    isMagicEffectActive = true,
                    magicAnimationTag = "MAGIC_ACTIVE_$playerId"
                )
            }.copy(logMessage = "✨ ${player.name} activated Magic Boost! Next roll is supercharged!")
        }

        soundManager.playMagic(repository.userAccount.value.soundEnabled)
        syncRoomTurn(_gameState.value)
    }

    private fun launchPlayerMagicCooldown(playerId: String) {
        magicCooldownJobs[playerId]?.cancel()
        magicCooldownJobs[playerId] = viewModelScope.launch {
            for (sec in 30 downTo 1) {
                delay(1000)
                _gameState.update { s ->
                    s.updatePlayerControl(playerId) { ctrl ->
                        ctrl.copy(magicCooldownSeconds = sec - 1)
                    }
                }
            }
            _gameState.update { s ->
                s.updatePlayerControl(playerId) { ctrl ->
                    ctrl.copy(
                        magicButtonStatus = MagicButtonStatus.READY,
                        magicCooldownSeconds = 0,
                        isMagicEffectActive = false
                    )
                }
            }
        }
    }

    // Independent Player Chat Controls
    fun togglePlayerChat(playerId: String) {
        val player = _gameState.value.getPlayer(playerId) ?: return
        val myUserId = repository.userAccount.value.userId
        if (player.id != myUserId && !player.isBot) return

        _gameState.update { s ->
            val current = s.getPlayer(playerId)?.controlState?.isChatOpen ?: false
            s.updatePlayerControl(playerId) { ctrl ->
                ctrl.copy(isChatOpen = !current)
            }
        }
        soundManager.playClick(repository.userAccount.value.soundEnabled)
    }

    fun sendPlayerChatMessage(playerId: String, text: String? = null, emoji: String? = null) {
        val player = _gameState.value.getPlayer(playerId) ?: return
        val myUserId = repository.userAccount.value.userId
        if (player.id != myUserId && !player.isBot) return

        _gameState.update { s ->
            s.updatePlayerControl(playerId) { ctrl ->
                ctrl.copy(
                    isChatOpen = false,
                    lastChatMessage = text,
                    activeEmoji = emoji
                )
            }
        }
        soundManager.playClick(repository.userAccount.value.soundEnabled)
        syncRoomTurn(_gameState.value)

        // Clear chat bubble after 3 seconds for this player only
        chatBubbleJobs[playerId]?.cancel()
        chatBubbleJobs[playerId] = viewModelScope.launch {
            delay(3000)
            _gameState.update { s ->
                s.updatePlayerControl(playerId) { ctrl ->
                    ctrl.copy(lastChatMessage = null, activeEmoji = null)
                }
            }
        }
    }

    // Independent Player Microphone Controls
    fun togglePlayerMic(playerId: String) {
        val player = _gameState.value.getPlayer(playerId) ?: return
        val myUserId = repository.userAccount.value.userId
        if (player.id != myUserId && !player.isBot) return

        val newStatus = if (player.controlState.micStatus == PlayerMicStatus.MUTED) {
            PlayerMicStatus.UNMUTED
        } else {
            PlayerMicStatus.MUTED
        }

        _gameState.update { s ->
            s.updatePlayerControl(playerId) { ctrl ->
                ctrl.copy(micStatus = newStatus)
            }.copy(micEnabled = if (playerId == myUserId) newStatus != PlayerMicStatus.MUTED else s.micEnabled)
        }
        soundManager.playClick(repository.userAccount.value.soundEnabled)
        syncRoomTurn(_gameState.value)
    }

    fun toggleInGameMic() {
        // Toggles local player's mic
        val myUserId = repository.userAccount.value.userId
        val me = _gameState.value.players.firstOrNull { it.id == myUserId } ?: _gameState.value.players.firstOrNull()
        if (me != null) {
            togglePlayerMic(me.id)
        }
    }

    fun leaveGame() {
        turnTimerJob?.cancel()
        botTurnJob?.cancel()
        magicCooldownJobs.values.forEach { it.cancel() }
        magicCooldownJobs.clear()
        chatBubbleJobs.values.forEach { it.cancel() }
        chatBubbleJobs.clear()
        _currentScreen.value = AppScreen.HOME
    }

    // Voice Message Recording Simulation
    fun startVoiceRecording() {
        soundManager.playClick(repository.userAccount.value.soundEnabled)
        _isRecordingVoice.value = true
        _recordingDuration.value = 0
        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            while (_isRecordingVoice.value) {
                delay(1000)
                _recordingDuration.value += 1
            }
        }
    }

    fun stopAndSendVoiceRecording(friendId: String) {
        val duration = _recordingDuration.value.coerceAtLeast(1)
        _isRecordingVoice.value = false
        recordingJob?.cancel()
        repository.sendMessage(friendId, text = null, isVoice = true, voiceSec = duration)
    }

    fun cancelVoiceRecording() {
        _isRecordingVoice.value = false
        recordingJob?.cancel()
    }

    // Private Room Management
    fun generateNewPrivateRoomCode() {
        _privateRoomCode.value = "LD-" + Random.nextInt(100000, 999999).toString()
    }

    fun addBotToPrivateRoom() {
        if (_privateRoomPlayers.value.size >= 4) return
        val availableColors = PlayerColor.values().filterNot { c ->
            _privateRoomPlayers.value.any { it.color == c }
        }
        val nextColor = availableColors.firstOrNull() ?: return
        val bot = LudoPlayer(
            id = "bot_${Random.nextInt(100, 999)}",
            name = listOf("Falcon", "Viper", "Phoenix", "Blaze").random(),
            color = nextColor,
            isBot = true,
            level = Random.nextInt(8, 20)
        )
        _privateRoomPlayers.update { it + bot }
        soundManager.playClick(repository.userAccount.value.soundEnabled)
    }

    fun startPrivateRoomGame() {
        if (_privateRoomPlayers.value.size < 2) return
        initializeGame(GameMode.PRIVATE_ROOM, _privateRoomPlayers.value)
    }
}
