package de.dagadeta.schlauerbot

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import java.io.File
import java.util.*


class App {
    val greeting: String
        get() {
            return "Hello World!"
        }
}

fun main() {
    val props = Properties()
    val inputStream = File("config.properties").inputStream()
    inputStream.use {
        props.load(inputStream)
    }

    // Create a placeholder WordChecker
    val placeholderWordChecker = WordChecker(props.getProperty("dictionary.language"), Logging(null, 0, 0))
    val wordChainGame = WordChainGame(
        props.getProperty("wordChainGame.channelId").toLong(),
        props.getProperty("dictionary.language"),
        placeholderWordChecker
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
            WordChainCommandListener(wordChainGame)
        )
        .build()

    api.awaitReady()

    // Initialize Logging and WordChecker with the actual API instance
    val logging = Logging(
        api,
        props.getProperty("logging.guildId").toLong(),
        props.getProperty("logging.channelId").toLong()
    )
    val wordChecker = WordChecker(props.getProperty("dictionary.language"), logging)

    // Update the WordChainGame instance with the correct WordChecker
    wordChainGame.wordChecker = wordChecker

    logging.log("Bot started")

    configureDingDongCommands(api)
    configureWordChainCommands(api)
}