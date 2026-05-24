package de.dagadeta.schlauerbot.countinggame

import de.dagadeta.schlauerbot.common.FailureType.Critical
import de.dagadeta.schlauerbot.common.FailureType.Unspectacular
import de.dagadeta.schlauerbot.countinggame.CountingGame.CanNotCountFlag.RESET
import de.dagadeta.schlauerbot.countinggame.CountingGame.CanNotCountFlag.UNCHANGED
import de.dagadeta.schlauerbot.persistance.CountingGameState
import de.dagadeta.schlauerbot.persistance.CountingGameStatePersistenceService
import de.dagadeta.schlauerbot.persistance.UserState
import de.dagadeta.schlauerbot.persistance.UserStatePersistenceService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CountingGameTest {
    private val gameStateRepo = mock<CountingGameStatePersistenceService>()
    private val userStateRepo = mock<UserStatePersistenceService>()

    private val game = CountingGame(gameStateRepo, userStateRepo)

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
        val game = CountingGame(gameStateRepo, userStateRepo)

        assertThat(game.describeInitialState()).isEqualTo("Resuming CountingGame at 1955.")
    }

    @Test
    fun `failing to count sets canNotCount to true and resets streak`() {
        game.onMessageReceived(user2, 0)
        whenever(userStateRepo.findByIdOrNull(user1)).thenReturn(UserState(user1, 5, 5, false))

        game.onMessageReceived(user1, 7)

        val userStateCaptor = argumentCaptor<UserState>()
        verify(userStateRepo, atLeastOnce()).upsert(userStateCaptor.capture())
        assertThat(userStateCaptor.secondValue.streak).isEqualTo(0)
        assertThat(userStateCaptor.secondValue.longestStreak).isEqualTo(5)
        assertThat(userStateCaptor.secondValue.canNotCount).isTrue
    }

    @Test
    fun `starting the game with a wrong number also sets canNotCount to true`() {
        whenever(userStateRepo.findByIdOrNull(user1)).thenReturn(UserState(user1, 5, 5, false))

        game.onMessageReceived(user1, 1)

        val userStateCaptor = argumentCaptor<UserState>()
        verify(userStateRepo).upsert(userStateCaptor.capture())
        assertThat(userStateCaptor.firstValue.canNotCount).isTrue
    }

    @Test
    fun `succeeding to count increases streak and longestStreak`() {
        whenever(userStateRepo.findByIdOrNull(user1)).thenReturn(UserState(user1, 2, 2, false))

        val result = game.onMessageReceived(user1, 0)

        assertThat(result.getOrNull()).isEqualTo(UNCHANGED)
        val userStateCaptor = argumentCaptor<UserState>()
        verify(userStateRepo).upsert(userStateCaptor.capture())
        assertThat(userStateCaptor.firstValue.streak).isEqualTo(3)
        assertThat(userStateCaptor.firstValue.longestStreak).isEqualTo(3)
    }

    @Test
    fun `reaching the streak threshold resets canNotCount`() {
        whenever(userStateRepo.findByIdOrNull(user1)).thenReturn(UserState(user1, 9, 9, true))

        val result = game.onMessageReceived(user1, 0)

        assertThat(result.getOrNull()).isEqualTo(RESET)
        val userStateCaptor = argumentCaptor<UserState>()
        verify(userStateRepo).upsert(userStateCaptor.capture())
        assertThat(userStateCaptor.firstValue.canNotCount).isFalse
        assertThat(userStateCaptor.firstValue.streak).isEqualTo(10)
        assertThat(userStateCaptor.firstValue.longestStreak).isEqualTo(10)
    }

    @Test
    fun `streak increases but canNotCount remains false if not previously true`() {
        whenever(userStateRepo.findByIdOrNull(user1)).thenReturn(UserState(user1, 9, 9, false))

        val result = game.onMessageReceived(user1, 0)

        assertThat(result.getOrNull()).isEqualTo(UNCHANGED)
        val userStateCaptor = argumentCaptor<UserState>()
        verify(userStateRepo).upsert(userStateCaptor.capture())
        assertThat(userStateCaptor.firstValue.canNotCount).isFalse
        assertThat(userStateCaptor.firstValue.streak).isEqualTo(10)
    }
}
