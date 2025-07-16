package de.dagadeta.schlauerbot.wordchaingame

import de.dagadeta.schlauerbot.persistance.UsedWordRepository
import de.dagadeta.schlauerbot.persistance.WordChainGameStatePersistenceService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class WordChainGameTest {
    private val gameStateRepo = mock<WordChainGameStatePersistenceService>()
    private val usedWordRepo = mock<UsedWordRepository>()

    private val game = WordChainGame("en", { true }, gameStateRepo, usedWordRepo, true)

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
    fun `a paused game can be stopped`() {
        game.startGame()
        game.onMessageReceived("user-1", "flower")
        game.pauseGame()
        val message = game.stopGame()

        assertThat(message).isEqualTo("WordChainGame stopped! The next game will have a refreshed memory.")
    }

    @Test
    fun `a paused game can be started again`() {
        game.startGame()
        game.onMessageReceived("user-1", "unterflurhydrantenstraßenkappendeckelsteg")
        game.onMessageReceived("user-2", "grundflächenversiegelungsantragsbescheid")
        game.pauseGame()

        val message = game.startGame()

        assertThat(message).isEqualTo(
            """
                WordChainGame started with language "en"!
                
                HINT: The game still has 2 words in its memory. If you want to start a game without memory, use `/restart-word-chain-game`
            """.trimIndent()
        )
    }

    @Test
    fun `a game can always be restarted`() {
        assertThat(game.restartGame()).isEqualTo("WordChainGame restarted with a refreshed memory!")

        game.startGame()
        assertThat(game.restartGame()).isEqualTo("WordChainGame restarted with a refreshed memory!")

        game.onMessageReceived("user-1", "word")
        game.onMessageReceived("user-2", "desk")
        assertThat(game.restartGame()).isEqualTo("WordChainGame restarted with a refreshed memory!")

        game.stopGame()
        assertThat(game.restartGame()).isEqualTo("WordChainGame restarted with a refreshed memory!")
    }

    @Test
    fun `a word is not accepted on a not-yet started game`() {
        val result = game.onMessageReceived("user-1", "lollipop")

        assertThat(result.isFailure).isTrue
        assertThat(result.failureOrNull()).isEqualTo("WordChainGame is not started! Use `/start-word-chain-game` to start it")
    }

    @Test
    fun `the first word on a newly started game is accepted`() {
        game.startGame()

        val result = game.onMessageReceived("user-1", "lollipop")

        assertThat(result.isSuccess).isTrue
    }

    @Test
    fun `a subsequent word has to start with the last char of the previous`() {
        game.startGame()

        game.onMessageReceived("user-1", "lollipop")
        val result1 = game.onMessageReceived("user-2", "water")

        assertThat(result1.isFailure).isTrue
        assertThat(result1.failureOrNull()).isEqualTo("Word must start with the last letter of the last word which is 'p'!")

        val result2 = game.onMessageReceived("user-2", "plus")

        assertThat(result2.isSuccess).isTrue
    }

    @Test
    fun `all chars that should get accepted get accepted`() {
        game.startGame()
        assertThat(game.onMessageReceived("user-1", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ").isSuccess).isTrue

        game.restartGame()
        assertThat(game.onMessageReceived("user-1", "äöüÄÖÜßẞ").isSuccess).isTrue

        game.restartGame()
        assertThat(game.onMessageReceived("user-1", "áàâéèêíìîóòôúùûÁÀÂÉÈÊÍÌÎÓÒÔÚÙÛ").isSuccess).isTrue

        game.restartGame()
        assertThat(game.onMessageReceived("user-1", "çÇ").isSuccess).isTrue
    }

    @Test
    fun `equalised chars work`() {
        game.startGame()

        assertThat(game.onMessageReceived("user-1", "Spaß").isSuccess).isTrue
        assertThat(game.onMessageReceived("user-2", "Sau").isSuccess).isTrue
        assertThat(game.onMessageReceived("user-1", "über").isSuccess).isTrue
        assertThat(game.onMessageReceived("user-2", "REGARDÉ").isSuccess).isTrue
        assertThat(game.onMessageReceived("user-1", "être").isSuccess).isTrue
    }

    @Test
    fun `the case of the first and last char may differ`() {
        game.startGame()

        assertThat(game.onMessageReceived("user-1", "Lollipop").isSuccess).isTrue
        assertThat(game.onMessageReceived("user-2", "Plus").isSuccess).isTrue
        assertThat(game.onMessageReceived("user-1", "Salt").isSuccess).isTrue
        assertThat(game.onMessageReceived("user-2", "Tissue").isSuccess).isTrue
    }

    @Test
    fun `the same user is not allowed to write twice in a row`() {
        game.startGame()

        game.onMessageReceived("user-1", "lollipop")
        val result = game.onMessageReceived("user-1", "plus")

        assertThat(result.isFailure).isTrue
        assertThat(result.failureOrNull()).isEqualTo("You're not alone here! Let the others write words too!")
    }

    @Test
    fun `a too short word gets rejected`() {
        game.startGame()

        val result = game.onMessageReceived("user-1", "t")

        assertThat(result.isFailure).isTrue
        assertThat(result.failureOrNull()).isEqualTo("Word must be at least 2 characters long!")
    }

    @Test
    fun `a word consisting of the same letter gets rejected`() {
        game.startGame()

        val result = game.onMessageReceived("user-1", "aaaaaa")

        assertThat(result.isFailure).isTrue
        assertThat(result.failureOrNull()).isEqualTo("Word must not consist of the same letter repeated multiple times!")
    }

    @Test
    fun `a word containing illegal letters gets rejected`() {
        game.startGame()

        val result = game.onMessageReceived("user-1", "users'")

        assertThat(result.isFailure).isTrue
        assertThat(result.failureOrNull()).isEqualTo("Word must only contain valid letters!")
    }

    @Test
    fun `an already used word gets rejected`() {
        game.startGame()

        game.onMessageReceived("user-1", "aibohphobia")
        val result = game.onMessageReceived("user-2", "aibohphobia")

        assertThat(result.isFailure).isTrue
        assertThat(result.failureOrNull()).isEqualTo("Word already used in this round!")
    }

    @Test
    fun `an already used word with just different case gets rejected`() {
        game.startGame()

        game.onMessageReceived("user-1", "aibohphobia")
        val result = game.onMessageReceived("user-2", "aiBohPhoBia")

        assertThat(result.isFailure).isTrue
        assertThat(result.failureOrNull()).isEqualTo("Word already used in this round!")
    }

    @Test
    fun `an invalid word gets rejected`() {
        val game = WordChainGame("en", { false }, gameStateRepo, usedWordRepo, true)
        game.startGame()

        val result = game.onMessageReceived("user-1", "sdoitskl")

        assertThat(result.isFailure).isTrue
        assertThat(result.failureOrNull()).isEqualTo("Word does not exist in the configured dictionary!")
    }

    @Test
    fun `an invalid word is not rejected when wordChecker is disabled`() {
        val game = WordChainGame("en", { false }, gameStateRepo, usedWordRepo, false)
        game.startGame()

        val result = game.onMessageReceived("user-1", "sdoitskl")

        assertThat(result.isFailure).isFalse
        assertThat(result.failureOrNull()).isNull()
    }

    @Test
    fun `on a new game instance, describeInitialState() says the game is not yet started`() {
        assertThat(game.describeInitialState()).isEqualTo("WordChainGame is not yet started.")
    }

    @Test
    fun `on a started game, describeInitialState() says the game is started and has no words`() {
        game.startGame()
        assertThat(game.describeInitialState()).isEqualTo("WordChainGame is already started, but has no words in memory.")
    }

    @Test
    fun `on a started game with words, describeInitialState() returns the number of words and the last one`() {
        game.startGame()
        game.onMessageReceived("user-1", "something")
        assertThat(game.describeInitialState()).isEqualTo("""Resuming WordChainGame with 1 word(s) in memory. Last word was "something".""")
    }

    @Test
    fun `on a paused game with words, describeInitialState() returns the number of words and the last one`() {
        game.startGame()
        game.onMessageReceived("user-1", "something")
        game.pauseGame()
        assertThat(game.describeInitialState()).isEqualTo("""
            Resuming WordChainGame with 1 word(s) in memory. Last word was "something".
            Game paused.""".trimIndent())
    }
}
