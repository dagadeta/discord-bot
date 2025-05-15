package de.dagadeta.schlauerbot.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "bot-auth")
class BotAuthConfig(val token: String)

@ConfigurationProperties(prefix = "logging")
data class LoggingConfig(
    val guildId: Long,
    val channelId: Long,
)

@ConfigurationProperties(prefix = "word-chain-game")
data class WordChainGameConfig(
    val channelId: Long,
    val language: String,
)
