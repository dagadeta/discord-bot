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
        val dingDong = DingDongListener()

        val wordChainGame = DiscordWordChainGame(
            wordChainGameConfig.channelId,
            wordChainGameConfig.language,
            WiktionaryWordChecker(wordChainGameConfig.language, logging),
            gameStateRepo,
            usedWordRepo,
        )

        api.addEventListener(dingDong, wordChainGame)
        wordChainGame.writeInitialStateTo(logging)

        logging.log("Bot started")

        configureCommands(api, dingDong, wordChainGame)
    }

    private fun configureCommands(guild: JDA, vararg commandsProvider: WithSlashCommands) {
        guild.updateCommands().addCommands(
            commandsProvider.flatMap(WithSlashCommands::getSlashCommands)
        ).queue()
    }

    @PreDestroy
    fun logOnShutdown() = logging.log("Bot stopped")
}
