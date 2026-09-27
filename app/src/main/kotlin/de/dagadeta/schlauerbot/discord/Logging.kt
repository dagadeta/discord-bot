package de.dagadeta.schlauerbot.discord

import de.dagadeta.schlauerbot.config.LoggingConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA
import org.springframework.stereotype.Component

@Component
class Logging(private val guild: JDA?, private val config: LoggingConfig) {
    private val logger = KotlinLogging.logger {}

    fun info(message: () -> Any) {
        logger.info(message)
        if (guild != null) sendMessageToDiscordChannelById(config.channelId, message().toString())
    }

    fun warn(message: () -> Any?) {
        logger.warn(message)
        if (guild != null) sendMessageToDiscordChannelById(config.channelId, "**WARNING**: ${message()}")
    }

    fun error(throwable: Throwable?, message: () -> Any?) {
        logger.error(throwable, message)
        if (guild != null) sendMessageToDiscordChannelById(config.channelId, "**ERROR**: ${message()}")
    }

    private fun sendMessageToDiscordChannelById(channelId: String, message: String) {
        val channel = guild?.getGuildById(config.guildId)?.getTextChannelById(channelId)
        channel?.sendMessage(message)?.queue()
    }
}
