package de.dagadeta.schlauerbot.countinggame

import de.dagadeta.schlauerbot.persistance.CountingGameStatePersistenceService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class CountingGameTest {
    private val gameStateRepo = mock<CountingGameStatePersistenceService>()

    private val game = CountingGame(gameStateRepo)

    @Test
    fun `the first number is accepted`() {
        val result = game.onMessageReceived("voldemort", 0)

        assertThat(result.isSuccess).isTrue
    }

    @Test
    fun `starting the game with a wrong number is not accepted`() {
        val result = game.onMessageReceived("voldemort", 7)

        assertThat(result.isFailure).isTrue
        assertThat(result.failureOrNull()).isEqualTo("You didn't even manage to write the first number! Let's try that again (Tip: It's 0...)")
    }

    @Test
    fun `a correct number is accepted`() {
        game.onMessageReceived("voldemort", 0)
        val result = game.onMessageReceived("snape", 1)

        assertThat(result.isSuccess).isTrue
    }

    @Test
    fun `the same user is not allowed to write twice in a row`() {
        game.onMessageReceived("voldemort", 0)
        val result = game.onMessageReceived("voldemort", 1)

        assertThat(result.isFailure).isTrue
        assertThat(result.failureOrNull()).isEqualTo("You're not alone here! Let the others write numbers too!")
    }

    @Test
    fun `a wrong number is not accepted`() {
        game.onMessageReceived("voldemort", 0)
        val result = game.onMessageReceived("snape", 2)

        assertThat(result.isFailure).isTrue
        assertThat(result.failureOrNull()).isEqualTo("You RUINED it at 0! Let's start over with 0...")
    }

    @Test
    fun `resetting the game works`() {
        game.onMessageReceived("voldemort", 0)
        game.resetGame()
        val result = game.onMessageReceived("voldemort", 0)

        assertThat(result.isSuccess).isTrue
    }

    @Test
    fun `after a wrong number is written, the game is reset`() {
        game.onMessageReceived("voldemort", 0)
        game.onMessageReceived("snape", 2)
        val result = game.onMessageReceived("voldemort", 0)

        assertThat(result.isSuccess).isTrue
    }
}
