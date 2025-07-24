package de.dagadeta.schlauerbot

import de.dagadeta.schlauerbot.config.BotAuthConfig
import de.dagadeta.schlauerbot.config.DingDongConfig
import de.dagadeta.schlauerbot.config.LoggingConfig
import de.dagadeta.schlauerbot.config.WordChainGameConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableTransactionManagement
@EnableConfigurationProperties(
    BotAuthConfig::class,
    DingDongConfig::class,
    LoggingConfig::class,
    WordChainGameConfig::class,
)
class DiscordBotApplication

fun main(args: Array<String>) {
    runApplication<DiscordBotApplication>(*args)
}
