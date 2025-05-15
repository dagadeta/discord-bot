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
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
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
    val loggingConfig: LoggingConfig,
    val authConfig: BotAuthConfig,
    val gameStateRepo: WordChainGameStatePersistenceService,
    val usedWordRepo: UsedWordRepository,
) {
    private val logger = KotlinLogging.logger {}
    @PostConstruct
    fun startBot() {
        val wordChecker = WiktionaryWordChecker(wordChainGameConfig.language, Logging.INITIAL)
        val wordChainGame = DiscordWordChainGame(
            wordChainGameConfig.channelId,
            wordChainGameConfig.language,
            wordChecker,
            gameStateRepo,
            usedWordRepo,
        )
        val dingDong = DingDongListener()

        val api = JDABuilder
            .createLight(
                authConfig.token,
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
            loggingConfig.guildId,
            loggingConfig.channelId,
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
