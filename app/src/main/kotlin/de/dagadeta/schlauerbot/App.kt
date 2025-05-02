package de.dagadeta.schlauerbot

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import java.io.File
import java.util.*


fun main() {
    val props = Properties()
    val inputStream = File("config.properties").inputStream()
    inputStream.use {
        props.load(inputStream)
    }

    val wordChecker = WiktionaryWordChecker(props.getProperty("dictionary.language"), Logging.INITIAL)
    val wordChainGame = DiscordWordChainGame(
        props.getProperty("wordChainGame.channelId").toLong(),
        props.getProperty("dictionary.language"),
        wordChecker
    )

    val api = JDABuilder
        .createLight(
            props.getProperty("bot.token"),
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MEMBERS
        )
        .addEventListeners(
            DingDongListener(),
            wordChainGame,
        )
        .build()

    api.awaitReady()

    // Initialize Logging and WordChecker with the actual API instance
    val logging = Logging(
        api,
        props.getProperty("logging.guildId").toLong(),
        props.getProperty("logging.channelId").toLong()
    )

    // Update the WordChecker instance with the correct logging configuration
    wordChecker.logger = logging

    logging.log("Bot started")

    configureCommands(api)
}

fun configureCommands(guild: JDA) {
    guild.updateCommands().addCommands(
        Commands.slash("ding", "Answers Dong"),
        Commands.slash(startWordChainGameCommand, "Starts the WordChain game"),
        Commands.slash(stopWordChainGameCommand, "Stops the WordChain game (Memory will be cleared)"),
        Commands.slash(pauseWordChainGameCommand, "Pauses the WordChain game (Memory will remain)"),
        Commands.slash(restartWordChainGameCommand, "Restarts the WordChain game"),
    ).queue()
}