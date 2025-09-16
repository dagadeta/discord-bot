package de.dagadeta.schlauerbot.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "bot-auth")
class BotAuthConfig(val token: String)

@ConfigurationProperties(prefix = "logging")
data class LoggingConfig(
    val guildId: Long,
    val channelId: String,
)

@ConfigurationProperties(prefix = "admin")
data class AdminConfig(
    val roleId: String,
    val channelId: String,
)

@ConfigurationProperties(prefix = "word-checker")
data class WordCheckerConfig(
    val userAgent: String,
)
