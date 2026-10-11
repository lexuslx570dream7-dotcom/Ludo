package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUserProfile(
    var userId: String = "",
    var username: String = "",
    var avatar: String = "avatar_default",
    var level: Int = 12,
    var xp: Int = 320,
    var coins: Long = 2000L,
    var gems: Long = 4L,
    var totalGames: Int = 0,
    var wins: Int = 0,
    var losses: Int = 0,
    var winStreak: Int = 0,
    @ServerTimestamp var createdAt: Timestamp? = null,
    @ServerTimestamp var updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreGameRoom(
    var id: String = "",
    var mode: String = "FOUR_PLAYER",
    var status: String = "WAITING",
    var creatorId: String = "",
    var playerUids: List<String> = emptyList(),
    var currentTurnUid: String = "",
    var diceValue: Int = 1,
    var diceRolled: Boolean = false,
    var stateJson: String = "",
    var winnerUid: String = "",
    @ServerTimestamp var createdAt: Timestamp? = null,
    @ServerTimestamp var updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreTournament(
    var id: String = "",
    var name: String = "",
    var entryFeeCoins: Long = 500L,
    var prizeCoins: Long = 2000L,
    var maxParticipants: Int = 8,
    var participants: List<String> = emptyList(),
    var status: String = "ACTIVE",
    var winner: String = "",
    @ServerTimestamp var createdAt: Timestamp? = null,
    @ServerTimestamp var updatedAt: Timestamp? = null
)
