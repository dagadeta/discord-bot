package de.dagadeta.schlauerbot

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private const val channelId = 0L

class WordChainGameTest {

    val game = WordChainGame(channelId, "en") { true }

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

    @Nested
    inner class `When a user sends a message` {
        val messageReceived = mockk<MessageReceivedEvent>()
        val message = mockk<Message>(relaxed = true)

        @BeforeEach
        fun prepareMocks() {
            every { messageReceived.channel.id } returns "$channelId"
            every { messageReceived.author.isBot } returns false
            every { messageReceived.message } returns message
        }

        @Test
        fun `a word is not accepted on a not-yet started game`() {
            every { message.contentDisplay } returns "Lollipop"

            val result = game.onMessageReceived(messageReceived)

            assertThat(result.isFailure).isTrue
            assertThat(result.failureOrNull()).isEqualTo("WordChainGame is not started! Use `/start-word-chain-game` to start it")
        }

        @Test
        fun `the first word on a newly started game is accepted`() {
            game.startGame()
            every { message.contentDisplay } returns "Lollipop"

            game.onMessageReceived(messageReceived)

            verify { message.contentDisplay }
            confirmVerified(message)
        }
    }
}
