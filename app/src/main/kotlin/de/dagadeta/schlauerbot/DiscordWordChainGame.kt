package de.dagadeta.schlauerbot

import de.dagadeta.schlauerbot.WordChainGameCommand.Pause
import de.dagadeta.schlauerbot.WordChainGameCommand.Restart
import de.dagadeta.schlauerbot.WordChainGameCommand.Start
import de.dagadeta.schlauerbot.WordChainGameCommand.Stop
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands

class DiscordWordChainGame(channelId: Long, language: String, wordChecker: WordChecker) : ListenerAdapter(), WithSlashCommands {
    val game = WordChainGame(channelId, language, wordChecker)

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.name) {
            Start.command -> {
                event.deferReply().queue()
                game.startGame(event)
            }
            Stop.command -> {
                event.deferReply().queue()
                game.stopGame(event)
            }
            Pause.command -> {
                event.deferReply().queue()
                game.pauseGame(event)
            }
            Restart.command -> {
                event.deferReply().queue()
                game.restartGame(event)
            }
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) = game.onMessageReceived(event)

    override fun getSlashCommands() = WordChainGameCommand.entries.map {
        Commands.slash(it.command, it.description)
    }
}
