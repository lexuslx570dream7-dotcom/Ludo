package com.example.model

enum class PlayerColor(val displayName: String, val hexColor: Long) {
    RED("Red", 0xFFFF2A4B),
    GREEN("Green", 0xFF00C853),
    YELLOW("Yellow", 0xFFFFD600),
    BLUE("Blue", 0xFF00B0FF);

    fun next(): PlayerColor = when (this) {
        RED -> GREEN
        GREEN -> YELLOW
        YELLOW -> BLUE
        BLUE -> RED
    }
}

data class Pawn(
    val id: Int,
    val color: PlayerColor,
    // -1 = In base/yard
    // 0..50 = Common track relative to player's start offset
    // 51..55 = Player's colored home stretch path
    // 56 = Home (Safe and finished)
    val step: Int = -1
) {
    val isInBase: Boolean get() = step == -1
    val isFinished: Boolean get() = step >= 56
    val isInHomePath: Boolean get() = step in 51..55
    val isOnTrack: Boolean get() = step in 0..50
}

enum class MagicButtonStatus {
    READY,
    ACTIVE,
    COOLDOWN
}

enum class PlayerMicStatus {
    MUTED,
    UNMUTED,
    SPEAKING
}

enum class PlayerTurnStatus {
    WAITING,
    ACTIVE,
    TIMEOUT,
    FINISHED
}

enum class PlayerConnectionStatus {
    ONLINE,
    RECONNECTING,
    OFFLINE
}

data class PlayerControlState(
    val playerId: String,
    val magicButtonStatus: MagicButtonStatus = MagicButtonStatus.READY,
    val magicCooldownSeconds: Int = 0,
    val isMagicEffectActive: Boolean = false,
    val magicAnimationTag: String? = null,
    val isChatOpen: Boolean = false,
    val lastChatMessage: String? = null,
    val activeEmoji: String? = null,
    val micStatus: PlayerMicStatus = PlayerMicStatus.MUTED,
    val turnStatus: PlayerTurnStatus = PlayerTurnStatus.WAITING,
    val turnTimeRemaining: Int = 15,
    val connectionStatus: PlayerConnectionStatus = PlayerConnectionStatus.ONLINE
)

data class LudoPlayer(
    val id: String,
    val name: String,
    val color: PlayerColor,
    val pawns: List<Pawn> = (0..3).map { Pawn(id = it, color = color) },
    val isBot: Boolean = false,
    val rank: Int? = null,
    val level: Int = 12,
    val team: String? = null,
    val controlState: PlayerControlState = PlayerControlState(playerId = id)
) {
    val isMuted: Boolean get() = controlState.micStatus == PlayerMicStatus.MUTED
    val isSpeaking: Boolean get() = controlState.micStatus == PlayerMicStatus.SPEAKING
    val finishedPawnsCount: Int get() = pawns.count { it.isFinished }
    val isWinner: Boolean get() = finishedPawnsCount == 4
}

enum class GameMode(val title: String, val entryFee: Long, val prizePool: Long) {
    TWO_PLAYERS("2 Players Online", 500L, 950L),
    FOUR_PLAYERS("4 Players Match", 1000L, 3800L),
    TEAM_2V2("2v2 Team Battle", 1500L, 5700L),
    VS_COMPUTER("Play vs Computer", 0L, 250L),
    TOURNAMENT("Championship", 2000L, 10000L),
    PRIVATE_ROOM("Private Room", 500L, 1900L),
    LOCAL_PASS_AND_PLAY("Pass & Play", 0L, 0L),
    VIP_ROOM("VIP High Roller", 5000L, 19000L)
}

data class LudoGameState(
    val matchId: String = "MATCH-1001",
    val mode: GameMode = GameMode.FOUR_PLAYERS,
    val players: List<LudoPlayer> = emptyList(),
    val currentTurnIndex: Int = 0,
    val diceValue: Int? = null,
    val isRolling: Boolean = false,
    val canRoll: Boolean = true,
    val selectablePawnIds: List<Int> = emptyList(),
    val consecutiveSixes: Int = 0,
    val logMessage: String = "Welcome to Ludo Club!",
    val isGameOver: Boolean = false,
    val turnTimeRemaining: Int = 15,
    val voiceChatActive: Boolean = true,
    val micEnabled: Boolean = false
) {
    val currentPlayer: LudoPlayer?
        get() = if (players.isNotEmpty()) players.getOrNull(currentTurnIndex % players.size) else null

    val winner: LudoPlayer?
        get() = players.firstOrNull { it.isWinner }

    val diceRolled: Boolean
        get() = diceValue != null && !isRolling

    fun getPlayer(playerId: String): LudoPlayer? = players.firstOrNull { it.id == playerId }

    fun updatePlayerControl(playerId: String, transform: (PlayerControlState) -> PlayerControlState): LudoGameState {
        val updated = players.map { p ->
            if (p.id == playerId) p.copy(controlState = transform(p.controlState)) else p
        }
        return copy(players = updated)
    }
}

data class UserAccount(
    val userId: String = "#LD-849201",
    val username: String = "MirSanaullah",
    val level: Int = 12,
    val xp: Int = 3450,
    val maxXp: Int = 5000,
    val coins: Long = 2000L,
    val gems: Long = 4L,
    val totalGames: Int = 148,
    val wins: Int = 94,
    val losses: Int = 54,
    val winStreak: Int = 7,
    val musicEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
) {
    val winRate: Float get() = if (totalGames > 0) (wins.toFloat() / totalGames * 100f) else 0f
}

data class Friend(
    val id: String,
    val name: String,
    val level: Int,
    val isOnline: Boolean,
    val statusText: String,
    val avatarInitial: String
)

data class FriendRequest(
    val id: String,
    val friend: Friend,
    val timeAgo: String
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val text: String? = null,
    val voiceDurationSeconds: Int? = null,
    val isVoice: Boolean = false,
    val imageUrl: String? = null,
    val timestamp: String,
    val isMe: Boolean = false,
    val isRead: Boolean = true
)

data class Tournament(
    val id: String,
    val name: String,
    val entryFeeCoins: Long,
    val prizePoolCoins: Long,
    val currentPlayers: Int,
    val maxPlayers: Int,
    val startTime: String,
    val status: String, // "LIVE", "STARTING SOON", "COMPLETED"
    val tier: String = "Gold League"
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val currentProgress: Int,
    val maxProgress: Int,
    val isUnlocked: Boolean,
    val unlockedDate: String? = null
)
