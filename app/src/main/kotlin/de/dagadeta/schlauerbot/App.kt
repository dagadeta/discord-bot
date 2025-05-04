package de.dagadeta.schlauerbot

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
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
    val dingDong = DingDongListener()

    val api = JDABuilder
        .createLight(
            props.getProperty("bot.token"),
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MEMBERS
        )
        .addEventListeners(
            dingDong,
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
    logging.logOnShutdown("Bot stopped")

    configureCommands(api, dingDong, wordChainGame)
}

fun configureCommands(guild: JDA, vararg commandsProvider: WithSlashCommands) {
    guild.updateCommands().addCommands(
        commandsProvider.flatMap(WithSlashCommands::getSlashCommands)
    ).queue()
}
