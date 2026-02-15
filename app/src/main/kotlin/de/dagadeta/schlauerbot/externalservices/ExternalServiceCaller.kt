package de.dagadeta.schlauerbot.externalservices

import de.dagadeta.schlauerbot.botconfig.Bottalking
import de.dagadeta.schlauerbot.discord.Logging
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import java.lang.Thread.sleep

private val logger = KotlinLogging.logger {}

open class ExternalServiceCaller(
    private val bottalking: Bottalking,
    private val logging: Logging,
    private val api: JDA,
    private val answerProvider: AnswerProvider,
    private val commandName: String,
    private val serviceName: String?,
    private val serviceDescription: String,
) : ListenerAdapter() {

    @PostConstruct
    fun startListener() {
        api.addEventListener(this)
        api.upsertCommand(Commands.slash(commandName, serviceDescription)).queue()
        logging.log("$serviceName started.")
        if (bottalking.channelId.isEmpty()) {
            logging.log("WARNING: The bottalking channel ID is not yet configured. Use the `/config`-command to set it.")
        }
    }

    @PreDestroy
    fun stopListener() {
        api.removeEventListener(this)
        logging.log("$serviceName stopped.")
        sleep(2000) // give the asynchronous tasks time to finish before cutting the connection
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.channel.id != bottalking.channelId) return
        if (event.name != commandName) return

        logger.info { "received /$commandName" }
        event.deferReply().queue()

        val answer = answerProvider.getAnswer()
        event.hook.sendMessage(answer).queue()
    }
}
