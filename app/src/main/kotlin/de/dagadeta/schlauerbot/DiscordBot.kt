package de.dagadeta.schlauerbot

import de.dagadeta.schlauerbot.config.BotAuthConfig
import de.dagadeta.schlauerbot.config.LoggingConfig
import de.dagadeta.schlauerbot.config.WordChainGameConfig
import de.dagadeta.schlauerbot.dingdong.DingDongListener
import de.dagadeta.schlauerbot.discord.Logging
import de.dagadeta.schlauerbot.discord.WithSlashCommands
import de.dagadeta.schlauerbot.persistance.UsedWordRepository
import de.dagadeta.schlauerbot.persistance.WordChainGameStatePersistenceService
import de.dagadeta.schlauerbot.wordchaingame.DiscordWordChainGame
import de.dagadeta.schlauerbot.wordchaingame.WiktionaryWordChecker
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import net.dv8tion.jda.api.JDA
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.lang.Thread.sleep

@Component
@EnableConfigurationProperties(
    LoggingConfig::class,
    WordChainGameConfig::class,
    BotAuthConfig::class,
)
class DiscordBot(
    val wordChainGameConfig: WordChainGameConfig,
    val logging: Logging,
    val gameStateRepo: WordChainGameStatePersistenceService,
    val usedWordRepo: UsedWordRepository,
    val api: JDA,
) {
    @PostConstruct
    fun startBot() {
        val wordChainGame = DiscordWordChainGame(
            wordChainGameConfig.channelId,
            wordChainGameConfig.language,
            WiktionaryWordChecker(wordChainGameConfig.language, logging),
            gameStateRepo,
            usedWordRepo,
            wordChainGameConfig.checkWordExistence,
        )

        api.addEventListener(wordChainGame)
        wordChainGame.writeInitialStateTo(logging)

        logging.log("Bot started.")

        configureCommands(wordChainGame)
    }

    private fun configureCommands(vararg commandsProvider: WithSlashCommands) {
        api.updateCommands().addCommands(
            commandsProvider.flatMap(WithSlashCommands::getSlashCommands)
        ).queue()
    }

    @PreDestroy
    fun stopBot() {
        logging.log("Bot shutdown initiated. Removing all event listeners...")
        api.registeredListeners.forEach(api::removeEventListener)
        logging.log("Bot stopped. Shutting down.")
        sleep(2000) // give the asynchronous tasks time to finish before cutting the connection
    }
}
