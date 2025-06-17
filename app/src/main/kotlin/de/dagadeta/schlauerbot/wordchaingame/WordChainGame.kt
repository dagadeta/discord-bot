package de.dagadeta.schlauerbot.wordchaingame

import de.dagadeta.schlauerbot.common.Result
import de.dagadeta.schlauerbot.common.Result.Companion.failure
import de.dagadeta.schlauerbot.common.Result.Companion.success
import de.dagadeta.schlauerbot.persistance.UsedWord
import de.dagadeta.schlauerbot.persistance.UsedWordRepository
import de.dagadeta.schlauerbot.persistance.WordChainGameState
import de.dagadeta.schlauerbot.persistance.WordChainGameStatePersistenceService
import de.dagadeta.schlauerbot.wordchaingame.WordChainGameCommand.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class WordChainGame(
    private val language: String,
    private var wordChecker: WordChecker,
    private val gameStateRepo: WordChainGameStatePersistenceService,
    private val usedWordRepo: UsedWordRepository,
    private val checkWordExistence: Boolean,
) {
    private val minWordLength = 2
    private val wordRegex = Regex("^\\p{L}+$")
    private val theGameId = 0

    private val equalisedChars = mapOf(
        'ß' to 's', 'ẞ' to 's',
        'ä' to 'a', 'Ä' to 'a',
        'ö' to 'o', 'Ö' to 'o',
        'ü' to 'u', 'Ü' to 'u',

        'á' to 'a', 'Á' to 'a',
        'à' to 'a', 'À' to 'a',
        'é' to 'e', 'É' to 'e',
        'è' to 'e', 'È' to 'e',
        'í' to 'i', 'Í' to 'i',
        'ì' to 'i', 'Ì' to 'i',
        'ó' to 'o', 'Ó' to 'o',
        'ò' to 'o', 'Ò' to 'o',
        'ú' to 'u', 'Ú' to 'u',
        'ù' to 'u', 'Ù' to 'u',

        'â' to 'a', 'Â' to 'a',
        'ê' to 'e', 'Ê' to 'e',
        'î' to 'i', 'Î' to 'i',
        'ô' to 'o', 'Ô' to 'o',
        'û' to 'u', 'Û' to 'u',

        'ç' to 'c', 'Ç' to 'c',
    )

    private fun normalizeChar(c: Char): Char {
        return equalisedChars[c] ?: c.lowercaseChar()
    }

    private var started: Boolean = false
    private var lastUserId: String = ""
    private val usedWords: MutableList<String> = mutableListOf()

    init {
        gameStateRepo.findByIdOrNull(theGameId)?.let {
            started = it.started
            lastUserId = it.lastUser
        }

        usedWordRepo.findAll().forEach { usedWords.add(it.word) }
    }

    fun startGame(): String {
        if (started) {
            return "As WordChainGame is already started. Use `/${Stop.command}` to stop the game or `/${Restart.command}` to restart the game."
        }

        started = true
        saveState()
        logger.info { "WordChainGame started" }
        return "WordChainGame started with language \"$language\"!${if (usedWords.isNotEmpty()) "\n\nHINT: The game still has ${usedWords.size} words in its memory. If you want to start a game without memory, use `/${Restart.command}`" else ""}"
    }

    fun stopGame(): String {
        if (!started && usedWords.isEmpty()) {
            return "WordChainGame is already stopped!"
        }

        resetGame(false)

        logger.info { "WordChainGame stopped" }
        return "WordChainGame stopped! The next game will have a refreshed memory."
    }

    fun pauseGame(): String {
        if (!started) {
            return "WordChainGame is already paused or stopped!"
        }

        started = false
        saveState()
        return "WordChainGame paused!"
    }

    fun restartGame(): String {
        resetGame(true)
        logger.info { "WordChainGame restarted" }
        return "WordChainGame restarted with a refreshed memory!"
    }

    private fun resetGame(start: Boolean) {
        started = start
        lastUserId = ""
        usedWords.clear()
        saveState()
        usedWordRepo.deleteAll()
        logger.info { "WordChainGame memory cleared" }
    }

    fun onMessageReceived(userId: String, word: String): Result<Unit> = when {
        !started -> {
            failure("WordChainGame is not started! Use `/${Start.command}` to start it")
        }
        lastUserId == userId -> {
            failure("You're not alone here! Let the others write words too!")
        }
        word.length < minWordLength -> {
            failure("Word must be at least $minWordLength characters long!")
        }
        word.all { it == word[0] } -> {
            failure("Word must not consist of the same letter repeated multiple times!")
        }
        !wordRegex.matches(word) -> {
            failure("Word must only contain valid letters!")
        }
        usedWords.isNotEmpty() && normalizeChar(word.first()) != normalizeChar(usedWords.last().last()) -> {
            failure("Word must start with the last letter of the last word which is '${usedWords.last().last()}'!")
        }
        usedWords.contains(word) -> {
            failure("Word already used in this round!")
        }
        checkWordExistence && !wordChecker.isValidWord(word) -> {
            failure("Word does not exist in the configured dictionary!")
        }
        else -> {
            logger.info { "received WordChain word" }
            lastUserId = userId
            usedWords.add(word)

            saveState()
            usedWordRepo.save(UsedWord(word))

            success(Unit)
        }
    }

    fun describeInitialState(): String {
        return if (usedWords.isNotEmpty()) {
            """
                Resuming WordChainGame with ${usedWords.size} word(s) in memory. Last word was "${usedWords.last()}".
                ${if (!started) "Game paused." else ""}
            """.trimIndent().trim()
        } else {
            if (started) "WordChainGame is already started, but has no words in memory." else "WordChainGame is not yet started."
        }
    }

    private fun saveState() = gameStateRepo.upsert(WordChainGameState(theGameId, started, lastUserId))
}

enum class WordChainGameCommand(val command: String, val description: String) {
    Start("start-word-chain-game", "Starts the WordChain game"),
    Stop("stop-word-chain-game", "Stops the WordChain game (Memory will be cleared)"),
    Pause("pause-word-chain-game", "Pauses the WordChain game (Memory will remain)"),
    Restart("restart-word-chain-game", "Restarts the WordChain game"),
}