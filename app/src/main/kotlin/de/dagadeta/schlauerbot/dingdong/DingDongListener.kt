package de.dagadeta.schlauerbot.dingdong

import de.dagadeta.schlauerbot.botconfig.Bottalking
import de.dagadeta.schlauerbot.discord.Logging
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.springframework.stereotype.Service
import java.lang.Thread.sleep

private val logger = KotlinLogging.logger {}
private const val DING_COMMAND_NAME = "ding"

@Service
class DingDongListener(
    private val bottalking: Bottalking,
    private val logging: Logging,
    private val api: JDA,
) : ListenerAdapter() {

    @PostConstruct
    fun startListener() {
        api.addEventListener(this)
        api.upsertCommand(Commands.slash(DING_COMMAND_NAME, "Answers Dong")).queue()
        logging.log("${DingDongListener::class.simpleName} started.")
    }

    @PreDestroy
    fun stopListener() {
        api.removeEventListener(this)
        logging.log("${DingDongListener::class.simpleName} stopped.")
        sleep(2000) // give the asynchronous tasks time to finish before cutting the connection
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.channel.id != bottalking.channelId) return
        if (event.name != DING_COMMAND_NAME) return

        logger.info { "received !ding" }
        event.deferReply().queue()
        event.hook.sendMessage("Dong!").queue()
    }
}
