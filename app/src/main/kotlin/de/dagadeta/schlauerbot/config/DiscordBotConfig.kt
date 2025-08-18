package de.dagadeta.schlauerbot.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "bot-auth")
class BotAuthConfig(val token: String)

@ConfigurationProperties(prefix = "logging")
data class LoggingConfig(
    val guildId: Long,
    val channelId: String,
)
