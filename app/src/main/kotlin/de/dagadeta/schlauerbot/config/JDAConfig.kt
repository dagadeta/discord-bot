package de.dagadeta.schlauerbot.config

import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JDAConfig {
    private val logger = KotlinLogging.logger {}

    @Bean
    fun JDA(authConfig: BotAuthConfig): JDA {
        if (authConfig.token == "offline") {
            logger.warn {
                """
                    No token to authenticate with a discord server provided. Shutting down.
                    Configure the discord server and the channels to use in config/application.yml
                    See the project's README.md for details.
                """.trimIndent()
            }
        }

        return JDABuilder
            .createLight(
                authConfig.token,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MEMBERS
            )
            .setEnableShutdownHook(false) // handle shutdown explicitly in [DiscordBot]
            .build()
            .awaitReady()
    }
}
