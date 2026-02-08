package de.dagadeta.schlauerbot.couldyou

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
private const val COULD_YOU_COMMAND_NAME = "could-you"

@Service
class CouldYou(
    private val bottalking: Bottalking,
    private val logging: Logging,
    private val api: JDA,
    private val couldYouGetter: CouldYouGetter,
) : ListenerAdapter() {

    @PostConstruct
    fun startListener() {
        api.addEventListener(this)
        api.upsertCommand(Commands.slash(COULD_YOU_COMMAND_NAME, "No, but creative")).queue()
        logging.log("${CouldYou::class.simpleName} started.")
        if (bottalking.channelId.isEmpty()) {
            logging.log("WARNING: The bottalking channel ID is not yet configured. Use the `/config`-command to set it.")
        }
    }

    @PreDestroy
    fun stopListener() {
        api.removeEventListener(this)
        logging.log("${CouldYou::class.simpleName} stopped.")
        sleep(2000) // give the asynchronous tasks time to finish before cutting the connection
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.channel.id != bottalking.channelId) return
        if (event.name != COULD_YOU_COMMAND_NAME) return

        logger.info { "received /could-you" }
        event.deferReply().queue()

        val reason = couldYouGetter.getRejectionReason()
        event.hook.sendMessage(reason).queue()
    }
}