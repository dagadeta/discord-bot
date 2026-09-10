package de.dagadeta.schlauerbot.countinggame

import de.dagadeta.schlauerbot.common.FailureType.Critical
import de.dagadeta.schlauerbot.common.FailureType.Unspectacular
import de.dagadeta.schlauerbot.countinggame.CountingGame.CanNotCountFlag.RESET
import de.dagadeta.schlauerbot.countinggame.CountingGame.CanNotCountFlag.UNCHANGED
import de.dagadeta.schlauerbot.countinggame.CountingGame.CountingGameLeaderboard
import de.dagadeta.schlauerbot.countinggame.CountingGame.CountingGameLeaderboard.CorrectNumbers
import de.dagadeta.schlauerbot.countinggame.CountingGame.CountingGameLeaderboard.CurrentStreak
import de.dagadeta.schlauerbot.countinggame.CountingGame.CountingGameLeaderboard.LongestStreak
import de.dagadeta.schlauerbot.persistance.CountingGameState
import de.dagadeta.schlauerbot.persistance.CountingGameStatePersistenceService
import de.dagadeta.schlauerbot.persistance.UserState
import de.dagadeta.schlauerbot.persistance.UserStatePersistenceService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CountingGameTest {
    private val gameStateRepo = mock<CountingGameStatePersistenceService>()
    private val userStateRepo = mock<UserStatePersistenceService>()
    private val canNotCountResetThreshold = 10
    private val oneBeforeCanNotCountResetThreshold = canNotCountResetThreshold - 1

    private val game = CountingGame(gameStateRepo, userStateRepo, canNotCountResetThreshold)

    private val user1 = "Voldemort"
    private val user2 = "Snape"

    @Test
    fun `the first number is accepted`() {
        val result = game.onMessageReceived(user1, 0)

        assertThat(result.isSuccess).isTrue
    }

    @Test
    fun `starting the game with a wrong number is not accepted`() {
        val result = game.onMessageReceived(user1, 7)

        assertThat(result.isFailure).isTrue
        assertThat(result.failureOrNull()).isEqualTo("You didn't even manage to write the first number! Let's try that again (Tip: It's 0...)" to Unspectacular)
    }

    @Test
    fun `a correct number is accepted`() {
        game.onMessageReceived(user1, 0)
        val result = game.onMessageReceived(user2, 1)

        assertThat(result.isSuccess).isTrue
    }

    @Test
    fun `the same user is not allowed to write twice in a row`() {
        game.onMessageReceived(user1, 0)
        val result = game.onMessageReceived(user1, 1)

        assertThat(result.isFailure).isTrue
        assertThat(result.failureOrNull()).isEqualTo("You're not alone here! Let the others write numbers too!" to Unspectacular)
    }

    @Test
    fun `a wrong number is not accepted`() {
        game.onMessageReceived(user1, 0)
        val result = game.onMessageReceived(user2, 2)

        assertThat(result.isFailure).isTrue
        assertThat(result.failureOrNull()).isEqualTo("You RUINED it at 0! Let's start over with 0..." to Critical)
    }

    @Test
    fun `resetting the game works`() {
        game.onMessageReceived(user1, 0)
        game.resetGame()
        val result = game.onMessageReceived(user1, 0)

        assertThat(result.isSuccess).isTrue
    }

    @Test
    fun `after a wrong number is written, the game is reset`() {
        game.onMessageReceived(user1, 0)
        game.onMessageReceived(user2, 2)
        val result = game.onMessageReceived(user1, 0)

        assertThat(result.isSuccess).isTrue
    }

    @Test
    fun `the game state can be restored`() {
        whenever(gameStateRepo.findByIdOrNull(0)).thenReturn(CountingGameState(0, 1955, "MartyMcFly"))
        val game = CountingGame(gameStateRepo, userStateRepo, canNotCountResetThreshold)

        assertThat(game.describeInitialState()).isEqualTo("Resuming CountingGame at 1955.")
    }

    @Test
    fun `failing to count sets canNotCount to true and resets streak`() {
        game.onMessageReceived(user2, 0)
        whenever(userStateRepo.findByIdOrNull(user1)).thenReturn(UserState(user1, 5, 5, false, 0))

        game.onMessageReceived(user1, 7)

        val userStateCaptor = argumentCaptor<UserState>()
        verify(userStateRepo, atLeastOnce()).upsert(userStateCaptor.capture())
        assertThat(userStateCaptor.secondValue.streak).isEqualTo(0)
        assertThat(userStateCaptor.secondValue.longestStreak).isEqualTo(5)
        assertThat(userStateCaptor.secondValue.canNotCount).isTrue
    }

    @Test
    fun `failing to count does not change correctNumbersAmount`() {
        game.onMessageReceived(user2, 0)
        whenever(userStateRepo.findByIdOrNull(user1)).thenReturn(UserState(user1, 5, 5, false, 10))

        game.onMessageReceived(user1, 7)

        val userStateCaptor = argumentCaptor<UserState>()
        verify(userStateRepo, atLeastOnce()).upsert(userStateCaptor.capture())
        assertThat(userStateCaptor.secondValue.correctNumbersAmount).isEqualTo(10)
    }

    @Test
    fun `starting the game with a wrong number also sets canNotCount to true`() {
        whenever(userStateRepo.findByIdOrNull(user1)).thenReturn(UserState(user1, 5, 5, false, 0))

        game.onMessageReceived(user1, 1)

        val userStateCaptor = argumentCaptor<UserState>()
        verify(userStateRepo).upsert(userStateCaptor.capture())
        assertThat(userStateCaptor.firstValue.canNotCount).isTrue
    }

    @Test
    fun `succeeding to count increases streak, longestStreak and correctNumbersAmount`() {
        whenever(userStateRepo.findByIdOrNull(user1)).thenReturn(UserState(user1, 2, 2, false, 10))

        val result = game.onMessageReceived(user1, 0)

        assertThat(result.getOrNull()).isEqualTo(UNCHANGED)
        val userStateCaptor = argumentCaptor<UserState>()
        verify(userStateRepo).upsert(userStateCaptor.capture())
        assertThat(userStateCaptor.firstValue.streak).isEqualTo(3)
        assertThat(userStateCaptor.firstValue.longestStreak).isEqualTo(3)
        assertThat(userStateCaptor.firstValue.correctNumbersAmount).isEqualTo(11)
    }

    @Test
    fun `reaching the streak threshold resets canNotCount`() {
        whenever(userStateRepo.findByIdOrNull(user1)).thenReturn(UserState(user1, oneBeforeCanNotCountResetThreshold, oneBeforeCanNotCountResetThreshold, true, 0))

        val result = game.onMessageReceived(user1, 0)

        assertThat(result.getOrNull()).isEqualTo(RESET)
        val userStateCaptor = argumentCaptor<UserState>()
        verify(userStateRepo).upsert(userStateCaptor.capture())
        assertThat(userStateCaptor.firstValue.canNotCount).isFalse
        assertThat(userStateCaptor.firstValue.streak).isEqualTo(canNotCountResetThreshold)
        assertThat(userStateCaptor.firstValue.longestStreak).isEqualTo(canNotCountResetThreshold)
    }

    @Test
    fun `reaching the streak threshold with longest streak does not reset canNotCount`() {
        whenever(userStateRepo.findByIdOrNull(user1)).thenReturn(UserState(user1, canNotCountResetThreshold - 2, canNotCountResetThreshold, true, 0))

        val result = game.onMessageReceived(user1, 0)

        assertThat(result.getOrNull()).isEqualTo(UNCHANGED)
        val userStateCaptor = argumentCaptor<UserState>()
        verify(userStateRepo).upsert(userStateCaptor.capture())
        assertThat(userStateCaptor.firstValue.canNotCount).isTrue
        assertThat(userStateCaptor.firstValue.streak).isEqualTo(oneBeforeCanNotCountResetThreshold)
        assertThat(userStateCaptor.firstValue.longestStreak).isEqualTo(canNotCountResetThreshold)
    }

    @Test
    fun `streak increases but canNotCount remains false if not previously true`() {
        whenever(userStateRepo.findByIdOrNull(user1)).thenReturn(UserState(user1, oneBeforeCanNotCountResetThreshold, oneBeforeCanNotCountResetThreshold, false, 0))

        val result = game.onMessageReceived(user1, 0)

        assertThat(result.getOrNull()).isEqualTo(UNCHANGED)
        val userStateCaptor = argumentCaptor<UserState>()
        verify(userStateRepo).upsert(userStateCaptor.capture())
        assertThat(userStateCaptor.firstValue.canNotCount).isFalse
        assertThat(userStateCaptor.firstValue.streak).isEqualTo(canNotCountResetThreshold)
    }

    @Test
    fun `generateStatsMessage generates a correct stats message`() {
        whenever(userStateRepo.findByIdOrNull(user1)).thenReturn(UserState(user1, 5, 22, false, 1337))
        val message = game.generateStatsMessage(user1)
        assertThat(message).isEqualTo("""
            Streak: 5
            Longest Streak: 22
            Total Correct Numbers: 1337
        """.trimIndent())
    }

    @Test
    fun `generateStatsMessage generates a correct stats message when there is no streak yet`() {
        whenever(userStateRepo.findByIdOrNull(user1)).thenReturn(UserState(user1, 0, 0, false, 0))
        val message = game.generateStatsMessage(user1)
        assertThat(message).isEqualTo("""
            Streak: 0
            Longest Streak: 0
            Total Correct Numbers: 0
        """.trimIndent())
    }

    @Test
    fun `generateStatsMessage generates a correct stats message when there is no user state yet`() {
        val message = game.generateStatsMessage(user1)
        assertThat(message).isEqualTo("You haven't counted yet! 🥲")
    }

    @Nested
    inner class WithUserStates {
        val userStates = listOf(
            UserState("user1", 10, 20, false, 100),
            UserState("user2", 5, 25, false, 200),
            UserState("user3", 15, 15, false, 300),
            UserState("user4", 0, 0, false, 0),
            UserState("user5", 1, 1, false, 1),
            UserState("user6", 2, 2, false, 2),
            UserState("user7", 3, 3, false, 3),
            UserState("user8", 4, 4, false, 4),
            UserState("user9", 5, 5, false, 5),
            UserState("user10", 6, 6, false, 6),
            UserState("user11", 7, 7, false, 7)
        )

        @BeforeEach
        fun setUp() {
            whenever(userStateRepo.findAll()).thenReturn(userStates)
        }

        @Test
        fun `generateLeaderboardMessage generates a correct leaderboard message`() {
            val message = game.generateLeaderboardMessage("user4", CountingGameLeaderboard.entries)

            assertThat(message).contains("**Current Streaks**")
            assertThat(message).contains("1. <@user3> | 15")
            assertThat(message).contains("2. <@user1> | 10")
            assertThat(message).contains("...")
            assertThat(message).contains("11. <@user4> | 0")

            assertThat(message).contains("**Longest Streaks**")
            assertThat(message).contains("1. <@user2> | 25")
            assertThat(message).contains("2. <@user1> | 20")
            assertThat(message).contains("11. <@user4> | 0")

            assertThat(message).contains("**Total Correct Numbers**")
            assertThat(message).contains("1. <@user3> | 300")
            assertThat(message).contains("2. <@user2> | 200")
            assertThat(message).contains("3. <@user1> | 100")
        }

        @Test
        fun `generateLeaderboardMessage generates a correct Current Streaks leaderboard message`() {
            val message = game.generateLeaderboardMessage("user4", listOf(CurrentStreak))

            assertThat(message).contains("**Current Streaks**")
            assertThat(message).contains("1. <@user3> | 15")
            assertThat(message).contains("2. <@user1> | 10")
            assertThat(message).contains("...")
            assertThat(message).contains("11. <@user4> | 0")

            assertThat(message).doesNotContain("**Longest Streaks**")
            assertThat(message).doesNotContain("**Total Correct Numbers**")
        }

        @Test
        fun `generateLeaderboardMessage generates a correct Longest Streaks leaderboard message`() {
            val message = game.generateLeaderboardMessage("user4", listOf(LongestStreak))

            assertThat(message).contains("**Longest Streaks**")
            assertThat(message).contains("1. <@user2> | 25")
            assertThat(message).contains("2. <@user1> | 20")
            assertThat(message).contains("11. <@user4> | 0")

            assertThat(message).doesNotContain("**Current Streaks**")
            assertThat(message).doesNotContain("**Total Correct Numbers**")
        }

        @Test
        fun `generateLeaderboardMessage generates a correct Total Correct Numbers leaderboard message`() {
            val message = game.generateLeaderboardMessage("user4", listOf(CorrectNumbers))

            assertThat(message).contains("**Total Correct Numbers**")
            assertThat(message).contains("1. <@user3> | 300")
            assertThat(message).contains("2. <@user2> | 200")
            assertThat(message).contains("3. <@user1> | 100")

            assertThat(message).doesNotContain("**Current Streaks**")
            assertThat(message).doesNotContain("**Longest Streaks**")
        }
    }
}
