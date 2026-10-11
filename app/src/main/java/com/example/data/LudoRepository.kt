package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class LudoRepository {

    private val _userAccount = MutableStateFlow(
        UserAccount(
            userId = "#LD-849201",
            username = "MirSanaullah",
            level = 12,
            xp = 3450,
            maxXp = 5000,
            coins = 2000L,
            gems = 4L,
            totalGames = 148,
            wins = 94,
            losses = 54,
            winStreak = 7,
            musicEnabled = true,
            soundEnabled = true,
            vibrationEnabled = true
        )
    )
    val userAccount: StateFlow<UserAccount> = _userAccount.asStateFlow()

    private val _friends = MutableStateFlow(
        listOf(
            Friend("f1", "Alex Hunter", 14, true, "Ready to Play", "A"),
            Friend("f2", "Sarah Connor", 18, true, "In a VIP Match", "S"),
            Friend("f3", "David Miller", 9, false, "Offline 2h ago", "D"),
            Friend("f4", "Emma Watson", 22, true, "In Tournament", "E"),
            Friend("f5", "Rahul Sharma", 11, false, "Offline 1d ago", "R")
        )
    )
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    private val _friendRequests = MutableStateFlow(
        listOf(
            FriendRequest("req1", Friend("f6", "Zack Knight", 15, true, "Wants to be friends", "Z"), "10m ago"),
            FriendRequest("req2", Friend("f7", "Elena Rostova", 8, false, "Wants to be friends", "E"), "1h ago")
        )
    )
    val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(
        mapOf(
            "f1" to listOf(
                ChatMessage("m1", "f1", "Alex Hunter", text = "Hey Mir! Let's play a 2v2 match today!", timestamp = "11:20 AM", isMe = false),
                ChatMessage("m2", "me", "MirSanaullah", text = "Sure! Give me 5 minutes, finishing a game.", timestamp = "11:22 AM", isMe = true),
                ChatMessage("m3", "f1", "Alex Hunter", text = "I'll create the VIP room, send you code.", timestamp = "11:23 AM", isMe = false),
                ChatMessage("m4", "f1", "Alex Hunter", voiceDurationSeconds = 4, isVoice = true, timestamp = "11:24 AM", isMe = false)
            )
        )
    )
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    private val _tournaments = MutableStateFlow(
        listOf(
            Tournament("t1", "Mega Grand Championship", 2000L, 25000L, 28, 32, "Starts in 12m", "REGISTRATION_OPEN", "Diamond VIP"),
            Tournament("t2", "Super Star Blitz 4P", 1000L, 8000L, 16, 16, "Live Now", "LIVE", "Gold League"),
            Tournament("t3", "Weekend Knockout Cup", 500L, 5000L, 8, 16, "Starts at 6:00 PM", "UPCOMING", "Silver League")
        )
    )
    val tournaments: StateFlow<List<Tournament>> = _tournaments.asStateFlow()

    private val _achievements = MutableStateFlow(
        listOf(
            Achievement("a1", "First Victory", "Win your first Ludo game", "🏆", 1, 1, true, "Unlocked Oct 2"),
            Achievement("a2", "Ludo Master", "Win 10 matches online", "⭐", 10, 10, true, "Unlocked Oct 5"),
            Achievement("a3", "Half Century", "Win 50 matches online", "🔥", 50, 50, true, "Unlocked Oct 9"),
            Achievement("a4", "Centurion King", "Win 100 matches online", "👑", 94, 100, false),
            Achievement("a5", "High Roller", "Win a VIP Room match", "💎", 5, 5, true, "Unlocked Oct 8"),
            Achievement("a6", "Team Tactician", "Win 10 2v2 Team matches", "🛡️", 8, 10, false),
            Achievement("a7", "Tournament Titan", "Win any official Tournament", "🥇", 2, 3, false),
            Achievement("a8", "Lucky Sixes", "Roll three 6s in a row", "🎲", 1, 1, true, "Unlocked Oct 10")
        )
    )
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    // Currency Actions
    fun syncFromFirestore(
        userId: String,
        username: String,
        level: Int,
        coins: Long,
        gems: Long,
        totalGames: Int,
        wins: Int,
        losses: Int,
        winStreak: Int
    ) {
        _userAccount.update { current ->
            current.copy(
                userId = userId,
                username = username.ifBlank { current.username },
                level = level.coerceAtLeast(1),
                coins = coins,
                gems = gems,
                totalGames = totalGames,
                wins = wins,
                losses = losses,
                winStreak = winStreak
            )
        }
    }

    fun addCoins(amount: Long) {
        _userAccount.update { it.copy(coins = it.coins + amount) }
    }

    fun spendCoins(amount: Long): Boolean {
        if (_userAccount.value.coins >= amount) {
            _userAccount.update { it.copy(coins = it.coins - amount) }
            return true
        }
        return false
    }

    fun addGems(amount: Long) {
        _userAccount.update { it.copy(gems = it.gems + amount) }
    }

    fun spendGems(amount: Long): Boolean {
        if (_userAccount.value.gems >= amount) {
            _userAccount.update { it.copy(gems = it.gems - amount) }
            return true
        }
        return false
    }

    fun addGameStats(won: Boolean, coinsEarned: Long) {
        _userAccount.update { current ->
            val newWins = if (won) current.wins + 1 else current.wins
            val newLosses = if (!won) current.losses + 1 else current.losses
            val newStreak = if (won) current.winStreak + 1 else 0
            val addedXp = if (won) 150 else 40
            var newXp = current.xp + addedXp
            var newLevel = current.level
            if (newXp >= current.maxXp) {
                newXp -= current.maxXp
                newLevel += 1
            }
            current.copy(
                coins = current.coins + coinsEarned,
                totalGames = current.totalGames + 1,
                wins = newWins,
                losses = newLosses,
                winStreak = newStreak,
                xp = newXp,
                level = newLevel
            )
        }
    }

    fun updateSettings(music: Boolean? = null, sound: Boolean? = null, vibration: Boolean? = null, username: String? = null) {
        _userAccount.update { current ->
            current.copy(
                musicEnabled = music ?: current.musicEnabled,
                soundEnabled = sound ?: current.soundEnabled,
                vibrationEnabled = vibration ?: current.vibrationEnabled,
                username = username ?: current.username
            )
        }
    }

    fun acceptFriendRequest(requestId: String) {
        val req = _friendRequests.value.firstOrNull { it.id == requestId } ?: return
        _friendRequests.update { it.filterNot { item -> item.id == requestId } }
        _friends.update { it + req.friend.copy(statusText = "Online") }
    }

    fun rejectFriendRequest(requestId: String) {
        _friendRequests.update { it.filterNot { item -> item.id == requestId } }
    }

    fun addFriend(name: String) {
        val newFriend = Friend(
            id = "f_${UUID.randomUUID().toString().take(6)}",
            name = name.trim(),
            level = (5..25).random(),
            isOnline = true,
            statusText = "Just added",
            avatarInitial = name.firstOrNull()?.uppercase() ?: "U"
        )
        _friends.update { it + newFriend }
    }

    fun removeFriend(friendId: String) {
        _friends.update { it.filterNot { f -> f.id == friendId } }
    }

    fun sendMessage(friendId: String, text: String?, isVoice: Boolean = false, voiceSec: Int? = null, imageUrl: String? = null) {
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = "me",
            senderName = _userAccount.value.username,
            text = text,
            isVoice = isVoice,
            voiceDurationSeconds = voiceSec,
            imageUrl = imageUrl,
            timestamp = "Just now",
            isMe = true
        )
        val currentList = _messages.value[friendId] ?: emptyList()
        _messages.update { it + (friendId to (currentList + msg)) }
    }

    // Admin tournament control
    fun createTournament(name: String, entryFee: Long, prize: Long, maxPlayers: Int) {
        val tournament = Tournament(
            id = "t_${UUID.randomUUID().toString().take(5)}",
            name = name,
            entryFeeCoins = entryFee,
            prizePoolCoins = prize,
            currentPlayers = 1,
            maxPlayers = maxPlayers,
            startTime = "Starting Soon",
            status = "REGISTRATION_OPEN"
        )
        _tournaments.update { listOf(tournament) + it }
    }
}
