package de.dagadeta.schlauerbot

import de.dagadeta.schlauerbot.config.AdminConfig
import de.dagadeta.schlauerbot.config.BotAuthConfig
import de.dagadeta.schlauerbot.config.LoggingConfig
import de.dagadeta.schlauerbot.config.WordCheckerConfig
import org.mariuszgromada.math.mxparser.License
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableTransactionManagement
@EnableConfigurationProperties(
    BotAuthConfig::class,
    LoggingConfig::class,
    AdminConfig::class,
    WordCheckerConfig::class,
)
class DiscordBotApplication

fun main(args: Array<String>) {
    License.iConfirmNonCommercialUse("dagadeta")
    runApplication<DiscordBotApplication>(*args)
}
