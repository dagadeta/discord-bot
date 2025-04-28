package de.dagadeta.schlauerbot

import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

private val logger = KotlinLogging.logger {}

class WordChainGame(private val channelId: Long, private val language: String, var wordChecker: WordChecker) : ListenerAdapter() {
    private var started: Boolean = false
    private var lastWord: String = ""
    private var usedWords: MutableList<String> = mutableListOf()

    fun startGame(event: SlashCommandInteractionEvent) {
        val wasAlreadyStarted = started
        started = true

        if (wasAlreadyStarted) {
            clearMemory()
            event.hook.sendMessage("As WordChainGame was already started, I erased its word memory and restarted the game with language \"$language\"!").queue()
        } else event.hook.sendMessage("WordChainGame started with language \"$language\"!").queue()

        logger.info { "WordChainGame started" }
    }
    fun stopGame(event: SlashCommandInteractionEvent) {
        if (!started) {
            event.hook.sendMessage("WordChainGame is already stopped!").queue()
            return
        }

        clearMemory()

        started = false
        event.hook.sendMessage("WordChainGame stopped!").queue()
        logger.info { "WordChainGame stopped" }
    }
    private fun clearMemory() {
        lastWord = ""
        usedWords.clear()
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
        val word = message.contentDisplay

        if (!started) {
            sendInvalidWordMessage(message, "WordChainGame is not started! Use `/start-word-chain-game` to start it")
            return
        }
        if (word.length < 3) {
            sendInvalidWordMessage(message, "Word must be at least 3 characters long!")
            return
        }
        if (!Regex("^[a-zA-ZäöüÄÖÜß]+$").matches(word)) {
            sendInvalidWordMessage(message, "Word must only contain valid letters (a-z, äöüß)!")
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
        usedWords.add(word)
    }
}

const val startGameCommand = "start-word-chain-game"
const val stopGameCommand = "stop-word-chain-game"

class WordChainCommandListener(private val game: WordChainGame) : ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.name) {
            startGameCommand -> {
                event.deferReply().queue()
                game.startGame(event)
            }
            stopGameCommand -> {
                event.deferReply().queue()
                game.stopGame(event)
            }
        }
    }
}