package de.dagadeta.schlauerbot

import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA

private val logger = KotlinLogging.logger {}

class Logging(private val guild: JDA, private val guildId: Long, private val channelId: Long) {
    fun log(message: String) {
        logger.info { message }
        sendMessageToDiscordChannelById(guild, guildId, channelId, message)
    }

    private fun sendMessageToDiscordChannelById(guild: JDA, guildId: Long, channelId: Long, message: String) {
        val channel = guild.getGuildById(guildId)?.getTextChannelById(channelId)
        channel?.sendMessage(message)?.queue()
    }
}