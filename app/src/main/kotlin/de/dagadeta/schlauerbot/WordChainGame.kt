package de.dagadeta.schlauerbot

import de.dagadeta.schlauerbot.Result.Companion.failure
import de.dagadeta.schlauerbot.Result.Companion.success
import de.dagadeta.schlauerbot.WordChainGameCommand.Restart
import de.dagadeta.schlauerbot.WordChainGameCommand.Start
import de.dagadeta.schlauerbot.WordChainGameCommand.Stop
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

private val logger = KotlinLogging.logger {}

class WordChainGame(private val channelId: Long, private val language: String, private var wordChecker: WordChecker) {
    private var started: Boolean = false
    private var lastWord: String = ""
    private var lastUser: User? = null
    private val usedWords: MutableList<String> = mutableListOf()
    private var wordCount: Int = 0

    fun startGame(): String {
        if (started) {
            return "As WordChainGame is already started. Use `/${Stop.command}` to stop the game or `/${Restart.command}` to restart the game."
        }

        started = true
        logger.info { "WordChainGame started" }
        return "WordChainGame started with language \"$language\"!${ if(wordCount>0) "\n\nHINT: The game still has $wordCount words in its memory. If you want to start a game without memory, use `/${Restart.command}`" else ""}"
    }

    fun stopGame(): String {
        if (!started && wordCount == 0) {
            return "WordChainGame is already stopped!"
        }

        clearMemory()

        started = false
        logger.info { "WordChainGame stopped" }
        return "WordChainGame stopped! The next game will have a refreshed memory."
    }

    fun pauseGame(): String {
        if (!started) {
            return "WordChainGame is already paused or stopped!"
        }

        started = false
        return "WordChainGame paused!"
    }

    fun restartGame(): String {
        if (!started) { started = true }
        clearMemory()
        logger.info { "WordChainGame restarted" }
        return "WordChainGame restarted with a refreshed memory!"
    }

    private fun clearMemory() {
        lastWord = ""
        lastUser = null
        usedWords.clear()
        wordCount = 0
        logger.info { "WordChainGame memory cleared" }
    }

    fun onMessageReceived(event: MessageReceivedEvent ): Result<Unit> {
        val message = event.message

        if (!started) {
            return failure("WordChainGame is not started! Use `/${Start.command}` to start it")
        }
        if (lastUser != null && lastUser == event.author) {
            return failure( "You're not alone here! Let the others write words too!")
        }

        val word = message.contentDisplay

        if (word.length < 3) {
            return failure( "Word must be at least 3 characters long!")
        }
        if (!Regex("^[a-zA-ZäöüÄÖÜß]+$").matches(word)) {
            return failure( "Word must only contain valid letters (a-z, ä, ö, ü, ß)!")
        }
        if (lastWord.isNotEmpty() && word[0].uppercaseChar() != lastWord.last().uppercaseChar()) {
            return failure( "Word must start with the last letter of the last word!")
        }
        if (usedWords.contains(word)) {
            return failure( "Word already used in this round!")
        }
        if (!wordChecker.isValidWord(word)) {
            return failure( "Word does not exist in language \"$language\"!")
        }

        logger.info { "received WordChain word" }
        lastWord = word
        lastUser = event.author
        usedWords.add(word)
        wordCount++
        return success(Unit)
    }
}

enum class WordChainGameCommand(val command: String, val description: String) {
    Start("start-word-chain-game", "Starts the WordChain game"),
    Stop("stop-word-chain-game", "Stops the WordChain game (Memory will be cleared)"),
    Pause("pause-word-chain-game", "Pauses the WordChain game (Memory will remain)"),
    Restart("restart-word-chain-game", "Restarts the WordChain game"),
}
