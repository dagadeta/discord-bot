package de.dagadeta.schlauerbot

import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands

private val logger = KotlinLogging.logger {}

class WordChainGame(private val channelId: Long) : ListenerAdapter() {
    private var started: Boolean = false

    fun startGame(event: SlashCommandInteractionEvent) {
        started = true
        event.hook.sendMessage("WordChainGame started!").queue()
        logger.info { "WordChainGame started" }
    }

    fun stopGame(event: SlashCommandInteractionEvent) {
        started = false
        event.hook.sendMessage("WordChainGame stopped!").queue()
        logger.info { "WordChainGame stopped" }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channel.id.toLong() != channelId || event.author.isBot) return

        val message = event.message

        if (!started) {
            event.message.reply("WordChainGame is not started! Use `/start-word-chain-game` to start it").queue()
            return
        }

        logger.info { "received WordChain word" }
        message.reply("[DEBUGGING INFORMATION] I received your word!").queue()
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
                game.stopGame(event)
                event.reply("WordChainGame stopped!").queue()
            }
        }
    }
}

fun configureWordChainCommands(guild: JDA) {
    guild.updateCommands().addCommands(
        Commands.slash(startGameCommand, "Starts the WordChain game"),
        Commands.slash(stopGameCommand, "Stops the WordChain game"),
    ).queue()
}