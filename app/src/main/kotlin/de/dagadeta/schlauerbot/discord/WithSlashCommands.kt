package de.dagadeta.schlauerbot.discord

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface WithSlashCommands {
    fun getSlashCommands() : List<SlashCommandData>
}
