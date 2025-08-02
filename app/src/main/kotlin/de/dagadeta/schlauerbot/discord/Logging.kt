package de.dagadeta.schlauerbot.discord

import de.dagadeta.schlauerbot.config.LoggingConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA
import org.springframework.stereotype.Component

@Component
class Logging(private val guild: JDA?, private val config: LoggingConfig) {
    private val logger = KotlinLogging.logger {}

    fun log(message: String) {
        logger.info { message }
        if (guild != null) sendMessageToDiscordChannelById(config.channelId, message)
    }

    fun sendMessageToDiscordChannelById(channelId: String, message: String) {
        val channel = guild?.getGuildById(config.guildId)?.getTextChannelById(channelId)
        channel?.sendMessage(message)?.queue()
    }
}
