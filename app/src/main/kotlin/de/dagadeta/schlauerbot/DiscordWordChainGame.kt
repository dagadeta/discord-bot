package de.dagadeta.schlauerbot

import de.dagadeta.schlauerbot.WordChainGameCommand.*
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands

class DiscordWordChainGame(channelId: Long, language: String, wordChecker: WordChecker) : ListenerAdapter(), WithSlashCommands {
    val game = WordChainGame(channelId, language, wordChecker)

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        event.deferReply().queue()
        val message = when (event.name) {
            Start.command -> game.startGame()
            Stop.command -> game.stopGame()
            Pause.command -> game.pauseGame()
            Restart.command -> game.restartGame()
            else -> "Unknown command '${event.name}'"
        }
        event.hook.sendMessage(message).queue()
    }

    override fun onMessageReceived(event: MessageReceivedEvent) = game.onMessageReceived(event)

    override fun getSlashCommands() = WordChainGameCommand.entries.map {
        Commands.slash(it.command, it.description)
    }
}
