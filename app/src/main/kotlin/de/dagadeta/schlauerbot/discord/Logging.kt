package de.dagadeta.schlauerbot.discord

import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA

class Logging(private val guild: JDA?, private val guildId: Long, private val channelId: Long) {
    private val logger = KotlinLogging.logger {}

    fun log(message: String) {
        logger.info { message }
        if (guild != null) sendMessageToDiscordChannelById(channelId, message)
    }

    fun sendMessageToDiscordChannelById(channelId: Long, message: String) {
        val channel = guild?.getGuildById(guildId)?.getTextChannelById(channelId)
        channel?.sendMessage(message)?.queue()
    }

    fun logOnShutdown(message: String) {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() = log(message)
        })
    }

    companion object {
        val INITIAL = Logging(null, 0, 0)
    }
}