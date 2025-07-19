package de.dagadeta.schlauerbot.wordchaingame

import de.dagadeta.schlauerbot.common.onFailure
import de.dagadeta.schlauerbot.config.WordChainGameConfig
import de.dagadeta.schlauerbot.discord.Logging
import de.dagadeta.schlauerbot.persistance.UsedWordRepository
import de.dagadeta.schlauerbot.persistance.WordChainGameStatePersistenceService
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.springframework.stereotype.Service
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit

@Service
class DiscordWordChainGame(
    private val wordChainGameConfig: WordChainGameConfig,
    private val logging: Logging,
    private val api: JDA,
    gameStateRepo: WordChainGameStatePersistenceService,
    usedWordRepo: UsedWordRepository,
) : ListenerAdapter() {
    private val game = WordChainGame(
        wordChainGameConfig.language,
        WiktionaryWordChecker(wordChainGameConfig.language, logging),
        gameStateRepo,
        usedWordRepo,
        wordChainGameConfig.checkWordExistence
    )
    private val allCommandNames = WordChainGameCommand.entries.map(WordChainGameCommand::command)

    @PostConstruct
    fun startListener() {
        api.addEventListener(this)
        WordChainGameCommand.entries.forEach {
            api.upsertCommand(Commands.slash(it.command, it.description))
        }
        writeInitialStateTo(logging)

        logging.log("${DiscordWordChainGame::class.simpleName} started.")
    }

    @PreDestroy
    fun stopListener() {
        api.removeEventListener(this)
        logging.log("${DiscordWordChainGame::class.simpleName} stopped.")
        sleep(2000) // give the asynchronous tasks time to finish before cutting the connection
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        // don't react on unknown commands; there might be others that are not word chain game related
        if (event.name !in allCommandNames) return

        event.deferReply().queue()
        val message = when (event.name) {
            WordChainGameCommand.Start.command -> game.startGame()
            WordChainGameCommand.Stop.command -> game.stopGame()
            WordChainGameCommand.Pause.command -> game.pauseGame()
            WordChainGameCommand.Restart.command -> game.restartGame()
            else -> "Unknown command '${event.name}'"
        }
        event.hook.sendMessage(message).queue()
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channel.id.toLong() != wordChainGameConfig.channelId || event.author.isBot) return
        game.onMessageReceived(event.author.id, event.message.contentDisplay)
            .onFailure { answer -> sendInvalidWordMessage(event.message, answer) }
    }

    private fun sendInvalidWordMessage(originalMessage: Message, replyMessage: String) {
        fun temporaryReplyFallback() {
            originalMessage.reply(replyMessage).queue { reply ->
                originalMessage.delete().queueAfter(3, TimeUnit.SECONDS)
                reply.delete().queueAfter(3, TimeUnit.SECONDS)
            }
        }

        originalMessage.author.openPrivateChannel()
            .queue({ channel ->
                channel.sendMessage(replyMessage).queue(
                    { _ -> originalMessage.delete().queue() },
                    { _ -> temporaryReplyFallback() }
                )
            }, { _ ->
                temporaryReplyFallback()
            })
    }

    fun writeInitialStateTo(logging: Logging) {
        logging.log(game.describeInitialState())
    }
}
