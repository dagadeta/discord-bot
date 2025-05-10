package de.dagadeta.schlauerbot

import de.dagadeta.schlauerbot.persistance.UsedWordRepository
import de.dagadeta.schlauerbot.persistance.WordChainGameStatePersistenceService
import de.dagadeta.schlauerbot.wordchaingame.DiscordWordChainGame
import jakarta.annotation.PostConstruct
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.stereotype.Component
import java.io.File
import java.util.*

@Component
class DiscordBot(
    val gameStateRepo: WordChainGameStatePersistenceService,
    val usedWordRepo: UsedWordRepository
) {
    @PostConstruct
    fun startBot() {
        val props = Properties()
        val inputStream = File("config.properties").inputStream()
        inputStream.use {
            props.load(inputStream)
        }

        val wordChecker = WiktionaryWordChecker(props.getProperty("dictionary.language"), Logging.INITIAL)
        val wordChainGame = DiscordWordChainGame(
            props.getProperty("wordChainGame.channelId").toLong(),
            props.getProperty("dictionary.language"),
            wordChecker,
            gameStateRepo,
            usedWordRepo,
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

        wordChecker.logger = logging
        wordChainGame.writeInitialStateTo(logging)

        logging.log("Bot started")
        logging.logOnShutdown("Bot stopped")

        configureCommands(api, dingDong, wordChainGame)
    }

    private fun configureCommands(guild: JDA, vararg commandsProvider: WithSlashCommands) {
        guild.updateCommands().addCommands(
            commandsProvider.flatMap(WithSlashCommands::getSlashCommands)
        ).queue()
    }
}