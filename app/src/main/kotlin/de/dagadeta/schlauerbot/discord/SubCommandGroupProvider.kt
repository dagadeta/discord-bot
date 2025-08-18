package de.dagadeta.schlauerbot.discord

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData

interface SubCommandGroupProvider {
    val group: String

    fun getConfigureSubCommandGroup(): SubcommandGroupData
    fun onConfigureEvent(event: SlashCommandInteractionEvent)
}
