package de.dagadeta.schlauerbot

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WordChainGameTest {

    private val game = WordChainGame("en") { true }

    @Test
    fun `a game can be started`() {
        val message = game.startGame()

        assertThat(message).isEqualTo("WordChainGame started with language \"en\"!")
    }

    @Test
    fun `a game can not be started twice`() {
        game.startGame()
        val message = game.startGame()

        assertThat(message).isEqualTo("As WordChainGame is already started. Use `/stop-word-chain-game` to stop the game or `/restart-word-chain-game` to restart the game.")
    }

    @Test
    fun `a not running game can not be stopped`() {
        val message = game.stopGame()

        assertThat(message).isEqualTo("WordChainGame is already stopped!")
    }

    @Test
    fun `a started game can be stopped`() {
        game.startGame()
        val message = game.stopGame()

        assertThat(message).isEqualTo("WordChainGame stopped! The next game will have a refreshed memory.")
    }

    @Test
    fun `a not running game can not be paused`() {
        val message = game.pauseGame()

        assertThat(message).isEqualTo("WordChainGame is already paused or stopped!")
    }

    @Test
    fun `a started game can be paused`() {
        game.startGame()
        val message = game.pauseGame()

        assertThat(message).isEqualTo("WordChainGame paused!")
    }

    @Test
    fun `a paused game can be started again`() {
        game.startGame()

        // TODO: send some words as soon as onMessageReceived() doesn't need mocks anymore
        game.pauseGame()
        val message = game.startGame()

        assertThat(message).isEqualTo("WordChainGame started with language \"en\"!")
    }

    @Test
    fun `a game can always be restarted`() {
        assertThat(game.restartGame()).isEqualTo("WordChainGame restarted with a refreshed memory!")

        game.startGame()
        assertThat(game.restartGame()).isEqualTo("WordChainGame restarted with a refreshed memory!")

        // TODO: send some words as soon as onMessageReceived() doesn't need mocks anymore
        assertThat(game.restartGame()).isEqualTo("WordChainGame restarted with a refreshed memory!")

        game.stopGame()
        assertThat(game.restartGame()).isEqualTo("WordChainGame restarted with a refreshed memory!")
    }

    @Test
    fun `a word is not accepted on a not-yet started game`() {
        val result = game.onMessageReceived("user-1", "Lollipop")

        assertThat(result.isFailure).isTrue
        assertThat(result.failureOrNull()).isEqualTo("WordChainGame is not started! Use `/start-word-chain-game` to start it")
    }

    @Test
    fun `the first word on a newly started game is accepted`() {
        game.startGame()

        val result = game.onMessageReceived("user-1", "Lollipop")

        assertThat(result.isSuccess).isTrue
    }
}
