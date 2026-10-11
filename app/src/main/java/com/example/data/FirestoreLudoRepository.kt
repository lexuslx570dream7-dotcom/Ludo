package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreLudoRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance(db.app)

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- USER PROFILE OPERATIONS ---

    fun observeUserProfile(userId: String): Flow<FirestoreUserProfile?> {
        val docRef = db.collection("users").document(userId)
        return docRef.snapshots().map { snapshot ->
            if (snapshot.exists()) {
                snapshot.toObject(FirestoreUserProfile::class.java)
            } else {
                null
            }
        }
    }

    suspend fun getUserProfile(userId: String): FirestoreUserProfile? {
        val docRef = db.collection("users").document(userId)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) snapshot.toObject(FirestoreUserProfile::class.java) else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            null
        }
    }

    suspend fun saveUserProfile(profile: FirestoreUserProfile) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val path = docRef.path
        try {
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val data = mapOf(
                    "userId" to uid,
                    "username" to profile.username.ifBlank { "LudoMaster" },
                    "avatar" to profile.avatar,
                    "level" to profile.level.coerceAtLeast(1),
                    "xp" to profile.xp,
                    "coins" to profile.coins.coerceAtLeast(0L),
                    "gems" to profile.gems.coerceAtLeast(0L),
                    "totalGames" to profile.totalGames,
                    "wins" to profile.wins,
                    "losses" to profile.losses,
                    "winStreak" to profile.winStreak,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(data).await()
            } else {
                val data = mapOf(
                    "username" to profile.username.ifBlank { "LudoMaster" },
                    "avatar" to profile.avatar,
                    "level" to profile.level.coerceAtLeast(1),
                    "xp" to profile.xp,
                    "coins" to profile.coins.coerceAtLeast(0L),
                    "gems" to profile.gems.coerceAtLeast(0L),
                    "totalGames" to profile.totalGames,
                    "wins" to profile.wins,
                    "losses" to profile.losses,
                    "winStreak" to profile.winStreak,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(data).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    // --- GAME ROOM OPERATIONS ---

    suspend fun createGameRoom(room: FirestoreGameRoom): String {
        val uid = requireUserId()
        val roomRef = if (room.id.isNotBlank()) {
            db.collection("game_rooms").document(room.id)
        } else {
            db.collection("game_rooms").document()
        }
        val path = roomRef.path
        val generatedId = roomRef.id

        val payload = mapOf(
            "id" to generatedId,
            "mode" to room.mode,
            "status" to "WAITING",
            "creatorId" to uid,
            "playerUids" to listOf(uid),
            "currentTurnUid" to uid,
            "diceValue" to 1,
            "diceRolled" to false,
            "stateJson" to room.stateJson,
            "winnerUid" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            roomRef.set(payload).await()
            return generatedId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    fun observeGameRoom(roomId: String): Flow<FirestoreGameRoom?> {
        val docRef = db.collection("game_rooms").document(roomId)
        return docRef.snapshots().map { snapshot ->
            if (snapshot.exists()) {
                snapshot.toObject(FirestoreGameRoom::class.java)
            } else {
                null
            }
        }
    }

    fun observeWaitingRooms(): Flow<List<FirestoreGameRoom>> {
        val query = db.collection("game_rooms").whereEqualTo("status", "WAITING")
        return query.snapshots().map { snapshot ->
            snapshot.toObjects(FirestoreGameRoom::class.java)
        }
    }

    suspend fun updateGameRoomState(room: FirestoreGameRoom) {
        val docRef = db.collection("game_rooms").document(room.id)
        val path = docRef.path
        try {
            val updatePayload = mapOf(
                "playerUids" to room.playerUids,
                "status" to room.status,
                "currentTurnUid" to room.currentTurnUid,
                "diceValue" to room.diceValue,
                "diceRolled" to room.diceRolled,
                "stateJson" to room.stateJson,
                "winnerUid" to room.winnerUid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updatePayload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    suspend fun joinGameRoom(roomId: String) {
        val uid = requireUserId()
        val docRef = db.collection("game_rooms").document(roomId)
        val path = docRef.path
        try {
            docRef.update(
                "playerUids", FieldValue.arrayUnion(uid),
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    // --- TOURNAMENT OPERATIONS ---

    fun observeTournaments(): Flow<List<FirestoreTournament>> {
        val query = db.collection("tournaments")
        return query.snapshots().map { snapshot ->
            snapshot.toObjects(FirestoreTournament::class.java)
        }
    }

    suspend fun saveTournament(tournament: FirestoreTournament) {
        requireUserId()
        val docRef = db.collection("tournaments").document(tournament.id)
        val path = docRef.path
        val payload = mapOf(
            "id" to tournament.id,
            "name" to tournament.name,
            "entryFeeCoins" to tournament.entryFeeCoins,
            "prizeCoins" to tournament.prizeCoins,
            "maxParticipants" to tournament.maxParticipants,
            "participants" to tournament.participants,
            "status" to tournament.status,
            "winner" to tournament.winner,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }
}
