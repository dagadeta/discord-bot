package de.dagadeta.schlauerbot.dingdong

import de.dagadeta.schlauerbot.discord.WithSlashCommands
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands

private val logger = KotlinLogging.logger {}
const val dingCommand = "ding"

class DingDongListener : ListenerAdapter(), WithSlashCommands {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.channel.name != "\uD83E\uDD16｜bot-spielplatz") return
        if (event.name != dingCommand) return

        logger.info { "received !ding" }
        event.deferReply().queue()
        event.hook.sendMessage("Dong!").queue()
    }

    override fun getSlashCommands() = listOf(Commands.slash("ding", "Answers Dong"))
}
