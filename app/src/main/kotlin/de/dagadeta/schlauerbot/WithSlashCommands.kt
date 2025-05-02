package de.dagadeta.schlauerbot

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface WithSlashCommands {
    fun getSlashCommands() : List<SlashCommandData>
}
