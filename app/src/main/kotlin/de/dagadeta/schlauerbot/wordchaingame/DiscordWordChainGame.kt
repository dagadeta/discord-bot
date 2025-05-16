package de.dagadeta.schlauerbot.wordchaingame

import de.dagadeta.schlauerbot.discord.Logging
import de.dagadeta.schlauerbot.discord.WithSlashCommands
import de.dagadeta.schlauerbot.common.onFailure
import de.dagadeta.schlauerbot.persistance.UsedWordRepository
import de.dagadeta.schlauerbot.persistance.WordChainGameStatePersistenceService
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import java.util.concurrent.TimeUnit

class DiscordWordChainGame(
    private val channelId: Long,
    language: String,
    wordChecker: WordChecker,
    gameStateRepo: WordChainGameStatePersistenceService,
    usedWordRepo: UsedWordRepository,
) : ListenerAdapter(), WithSlashCommands {
    private val game = WordChainGame(language, wordChecker, gameStateRepo, usedWordRepo)
    private val allCommandNames = WordChainGameCommand.entries.map(WordChainGameCommand::command)

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
        if (event.channel.id.toLong() != channelId || event.author.isBot) return
        game.onMessageReceived(event.author.id, event.message.contentDisplay)
            .onFailure { answer -> sendInvalidWordMessage(event.message, answer) }
    }

    override fun getSlashCommands() = WordChainGameCommand.entries.map {
        Commands.slash(it.command, it.description)
    }

    private fun sendInvalidWordMessage(originalMessage: Message, replyMessage: String) {
        originalMessage.reply(replyMessage).queue { reply ->
            originalMessage.delete().queueAfter(3, TimeUnit.SECONDS)
            reply.delete().queueAfter(3, TimeUnit.SECONDS)
        }
    }

    fun writeInitialStateTo(logging: Logging) {
        logging.log(game.describeInitialState())
    }
}