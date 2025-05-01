package de.dagadeta.schlauerbot

import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

private val logger = KotlinLogging.logger {}

class WordChainGame(private val channelId: Long, private val language: String, private var wordChecker: WordChecker) : ListenerAdapter() {
    private var started: Boolean = false
    private var lastWord: String = ""
    private var lastUser: User? = null
    private val usedWords: MutableList<String> = mutableListOf()
    private var wordCount: Int = 0

    fun startGame(event: SlashCommandInteractionEvent) {
        if (started) {
            event.hook.sendMessage("As WordChainGame is already started. Use `/$stopWordChainGameCommand` to stop the game or `/$restartWordChainGameCommand` to restart the game.").queue()
            return
        }

        started = true
        event.hook.sendMessage("WordChainGame started with language \"$language\"!${ if(wordCount>0) "\n\nHINT: The game still has $wordCount words in its memory. If you want to start a game without memory, use `/$restartWordChainGameCommand`" else ""}").queue()
        logger.info { "WordChainGame started" }
    }
    fun stopGame(event: SlashCommandInteractionEvent) {
        if (!started && wordCount == 0) {
            event.hook.sendMessage("WordChainGame is already stopped!").queue()
            return
        }

        clearMemory()

        started = false
        event.hook.sendMessage("WordChainGame stopped! The next game will have a refreshed memory.").queue()
        logger.info { "WordChainGame stopped" }
    }
    fun pauseGame(event: SlashCommandInteractionEvent) {
        if (!started) {
            event.hook.sendMessage("WordChainGame is already paused or stopped!").queue()
            return
        }

        started = false
        event.hook.sendMessage("WordChainGame paused!").queue()
    }
    fun restartGame(event: SlashCommandInteractionEvent) {
        if (!started) { started = true }
        clearMemory()
        event.hook.sendMessage("WordChainGame restarted with a refreshed memory!").queue()
        logger.info { "WordChainGame restarted" }
    }
    private fun clearMemory() {
        lastWord = ""
        lastUser = null
        usedWords.clear()
        wordCount = 0
        logger.info { "WordChainGame memory cleared" }
    }

    private fun sendInvalidWordMessage(originalMessage: Message, replyMessage: String) {
        originalMessage.reply(replyMessage).queue { reply ->
            originalMessage.delete().queueAfter(3, java.util.concurrent.TimeUnit.SECONDS)
            reply.delete().queueAfter(3, java.util.concurrent.TimeUnit.SECONDS)
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channel.id.toLong() != channelId || event.author.isBot) return

        val message = event.message

        if (!started) {
            sendInvalidWordMessage(message, "WordChainGame is not started! Use `/$startWordChainGameCommand` to start it")
            return
        }
        if (lastUser != null && lastUser == event.author) {
            sendInvalidWordMessage(message, "You're not alone here! Let the others write words too!")
            return
        }

        val word = message.contentDisplay

        if (word.length < 3) {
            sendInvalidWordMessage(message, "Word must be at least 3 characters long!")
            return
        }
        if (!Regex("^[a-zA-ZäöüÄÖÜß]+$").matches(word)) {
            sendInvalidWordMessage(message, "Word must only contain valid letters (a-z, ä, ö, ü, ß)!")
            return
        }
        if (lastWord.isNotEmpty() && word[0].uppercaseChar() != lastWord.last().uppercaseChar()) {
            sendInvalidWordMessage(message, "Word must start with the last letter of the last word!")
            return
        }
        if (usedWords.contains(word)) {
            sendInvalidWordMessage(message, "Word already used in this round!")
            return
        }
        if (!wordChecker.isValidWord(word)) {
            sendInvalidWordMessage(message, "Word does not exist in language \"$language\"!")
            return
        }

        logger.info { "received WordChain word" }
        lastWord = word
        lastUser = event.author
        usedWords.add(word)
        wordCount++
    }
}

const val startWordChainGameCommand = "start-word-chain-game"
const val stopWordChainGameCommand = "stop-word-chain-game"
const val pauseWordChainGameCommand = "pause-word-chain-game"
const val restartWordChainGameCommand = "restart-word-chain-game"

class WordChainCommandListener(private val game: WordChainGame) : ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.name) {
            startWordChainGameCommand -> {
                event.deferReply().queue()
                game.startGame(event)
            }
            stopWordChainGameCommand -> {
                event.deferReply().queue()
                game.stopGame(event)
            }
            pauseWordChainGameCommand -> {
                event.deferReply().queue()
                game.pauseGame(event)
            }
            restartWordChainGameCommand -> {
                event.deferReply().queue()
                game.restartGame(event)
            }
        }
    }
}