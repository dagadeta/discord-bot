package de.dagadeta.schlauerbot

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class DiscordWordChainGame(channelId: Long, language: String, wordChecker: WordChecker) : ListenerAdapter() {
    val game = WordChainGame(channelId, language, wordChecker)

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

    override fun onMessageReceived(event: MessageReceivedEvent) = game.onMessageReceived(event)
}
