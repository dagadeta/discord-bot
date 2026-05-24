package de.dagadeta.schlauerbot.common

import net.dv8tion.jda.api.entities.Message
import java.util.concurrent.TimeUnit

fun sendPrivateMessage(message: Message, replyMessage: String) {
    fun temporaryReplyFallback() {
        message.reply(replyMessage).queue { reply ->
            message.delete().queueAfter(3, TimeUnit.SECONDS)
            reply.delete().queueAfter(3, TimeUnit.SECONDS)
        }
    }

    message.author.openPrivateChannel()
        .queue({ channel ->
            channel.sendMessage(replyMessage).queue(
                { message.delete().queue() },
                { temporaryReplyFallback() }
            )
        }, { temporaryReplyFallback() })
}
