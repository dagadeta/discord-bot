package de.dagadeta.schlauerbot

import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands

private val logger = KotlinLogging.logger {}

class DingDongListener : ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.channel.name != "\uD83E\uDD16｜bot-spielplatz") return

        val name = event.name

        if (name == "ding") {
            logger.info { "received !ding" }
            event.deferReply().queue()
            event.hook.sendMessage("Dong!").queue()
        }
    }
}

fun configureDingDongCommands(guild: JDA) {
    guild.updateCommands().addCommands(
        Commands.slash("ding", "Answers Dong")
    ).queue()
}