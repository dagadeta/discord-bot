package de.dagadeta.schlauerbot.discord

import de.dagadeta.schlauerbot.config.AdminConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

private val logger = KotlinLogging.logger {}

class PermissionValidator {
    fun sendWrongChannelMessage(event: SlashCommandInteractionEvent) {
        logger.info { "Wrong channel for command /${event.name} was used." }
        event.reply("This is the wrong channel for this command.").setEphemeral(true).queue()
    }

    fun sendNotEnoughRightsMessage(event: SlashCommandInteractionEvent) {
        logger.info { "User ${event.user.asTag} tried to use a command that requires more rights than they have." }
        event.reply("You don't have the rights to use this command.").setEphemeral(true).queue()
    }

    fun checkAdminAccess(event: SlashCommandInteractionEvent, adminConfig: AdminConfig): Boolean {
        if (event.member?.roles?.none { it.id == adminConfig.roleId } == true) {
            sendNotEnoughRightsMessage(event)
            return false
        }
        if (event.channel.id != adminConfig.channelId) {
            sendWrongChannelMessage(event)
            return false
        }
        return true
    }
}
