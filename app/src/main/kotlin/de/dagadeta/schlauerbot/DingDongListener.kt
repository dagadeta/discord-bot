package de.dagadeta.schlauerbot

import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

private val logger = KotlinLogging.logger {}

class DingDongListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channel.name != "\uD83E\uDD16｜bot-spielplatz") return
        if (event.author.isBot) return

        val message = event.message
        val content = message.contentRaw

        if (content == "!ding") {
            logger.info { "received !ding" }
            val channel: MessageChannel = event.channel
            channel.sendMessage("Dong!")
                .queue() // Important to call .queue() on the RestAction returned by sendMessage(...)
        }
    }
}
