package de.dagadeta.schlauerbot.discord

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Component
import kotlin.collections.get

@Component
class SubCommandEventDispatcher(
    private val api: JDA,
    listeners: List<SubCommandGroupProvider>,
) : ListenerAdapter() {

    private val listenersByGroup = listeners.associateBy { it.group }

    @PostConstruct
    fun startListener() = api.addEventListener(this)
    @PreDestroy
    fun stopListener() = api.removeEventListener(this)

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        listenersByGroup[event.interaction.subcommandGroup]?.onConfigureEvent(event)
    }
}
