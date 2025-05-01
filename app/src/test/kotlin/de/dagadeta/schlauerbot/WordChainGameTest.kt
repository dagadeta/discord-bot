package de.dagadeta.schlauerbot

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private const val channelId = 0L

class WordChainGameTest {

    val game = WordChainGame(channelId, "en") { true }

    val event = mockk<SlashCommandInteractionEvent>()
    val hook = mockk<InteractionHook>(relaxed = true)

    @BeforeEach
    fun prepareMocks() {
        every { event.hook } returns hook
    }

    @AfterEach
    fun `check if all recorded calls were verified`() {
        confirmVerified(hook)
    }

    @Test
    fun `a game can be started`() {
        game.startGame(event)

        verify { hook.sendMessage("WordChainGame started with language \"en\"!") }
    }

    @Test
    fun `a game can not be started twice`() {
        game.startGame(event)
        game.startGame(event)

        verify { hook.sendMessage("WordChainGame started with language \"en\"!") }
        verify { hook.sendMessage("As WordChainGame is already started. Use `/stop-word-chain-game` to stop the game or `/restart-word-chain-game` to restart the game.") }
    }

    @Test
    fun `a not running game can not be stopped`() {
        game.stopGame(event)

        verify { hook.sendMessage("WordChainGame is already stopped!") }
    }

    @Test
    fun `a started game can be stopped`() {
        game.startGame(event)
        game.stopGame(event)

        verify { hook.sendMessage("WordChainGame started with language \"en\"!") }
        verify { hook.sendMessage("WordChainGame stopped! The next game will have a refreshed memory.") }
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

            game.onMessageReceived(messageReceived)

            verify { message.reply("WordChainGame is not started! Use `/start-word-chain-game` to start it") }
        }

        @Test
        fun `the first word on a newly started game is accepted`() {
            game.startGame(event)
            every { message.contentDisplay } returns "Lollipop"

            game.onMessageReceived(messageReceived)

            verify { hook.sendMessage("WordChainGame started with language \"en\"!") }
            verify { message.contentDisplay }
            confirmVerified(message)
        }
    }
}
