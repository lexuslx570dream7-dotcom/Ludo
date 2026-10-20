package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LudoRulesTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveUserProfile_authenticatedUser_succeeds() {
        runBlocking {
            val uid = signInTestUser("player1@test.com")
            val repository = FirestoreLudoRepository(firestore)

            val profile = FirestoreUserProfile(
                userId = uid,
                username = "PlayerOne",
                level = 12,
                coins = 2000L,
                gems = 4L
            )

            withTimeout(5000L) {
                repository.saveUserProfile(profile)
            }

            val loaded = withTimeout(5000L) {
                repository.getUserProfile(uid)
            }

            assertNotNull(loaded)
            assertEquals("PlayerOne", loaded?.username)
            assertEquals(2000L, loaded?.coins)
        }
    }

    @Test
    fun createGameRoom_authenticatedUser_createsRoom() {
        runBlocking {
            val uid = signInTestUser("host@test.com")
            val repository = FirestoreLudoRepository(firestore)

            val room = FirestoreGameRoom(
                id = "room_test_1",
                mode = "FOUR_PLAYER",
                status = "WAITING",
                creatorId = uid,
                playerUids = listOf(uid)
            )

            val roomId = withTimeout(5000L) {
                repository.createGameRoom(room)
            }

            assertEquals("room_test_1", roomId)
        }
    }

    @Test
    fun unauthenticatedAccess_throwsException() {
        runBlocking {
            auth.signOut()
            val repository = FirestoreLudoRepository(firestore)

            assertThrows(IllegalStateException::class.java) {
                repository.requireUserId()
            }
        }
    }
}
