package de.dagadeta.schlauerbot

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WordChainGameTest {

    val wordChecker = mockk<WordChecker>()
    val game = WordChainGame(0, "en", wordChecker)

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
}
