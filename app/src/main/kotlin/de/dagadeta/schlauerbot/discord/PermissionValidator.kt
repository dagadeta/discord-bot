package de.dagadeta.schlauerbot.discord

import de.dagadeta.schlauerbot.config.AdminConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class PermissionValidator {
    fun checkCorrectChannel(event: SlashCommandInteractionEvent, channelId: String): Boolean {
        if (event.channel.id != channelId) {
            sendWrongChannelMessage(event, channelId)
            return false
        }
        return true
    }

    fun checkAdminAccess(event: SlashCommandInteractionEvent, adminConfig: AdminConfig): Boolean {
        if (event.member?.roles?.none { it.id == adminConfig.roleId } == true) {
            sendNotEnoughRightsMessage(event)
            return false
        }
        return checkCorrectChannel(event, adminConfig.channelId)
    }

    private fun sendWrongChannelMessage(event: SlashCommandInteractionEvent, channelId: String) {
        logger.info { "Wrong channel for command /${event.name} was used." }
        event.reply("This is the wrong channel for this command.\nCorrect channel: <#$channelId>").setEphemeral(true).queue()
    }

    private fun sendNotEnoughRightsMessage(event: SlashCommandInteractionEvent) {
        logger.info { "User ${event.user.asTag} tried to use a command that requires more rights than they have." }
        event.reply("You don't have the rights to use this command.").setEphemeral(true).queue()
    }
}
