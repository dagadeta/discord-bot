package de.dagadeta.schlauerbot.botconfig

import de.dagadeta.schlauerbot.discord.Logging
import de.dagadeta.schlauerbot.discord.SubCommandGroupProvider
import jakarta.annotation.PostConstruct
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("!integTest")
class BotConfiguration(
    private val api: JDA,
    private val logging: Logging,
    private val subCommandGroupProviders: List<SubCommandGroupProvider>,
) {
    @PostConstruct
    fun registerConfigCommand() {
        val configCommand = Commands.slash("config", "Configures the bot")
        subCommandGroupProviders.forEach {
            configCommand.addSubcommandGroups(it.getConfigureSubCommandGroup())
        }
        api.upsertCommand(configCommand).queue()
        logging.log("${BotConfiguration::class.simpleName} started.")
    }
}
