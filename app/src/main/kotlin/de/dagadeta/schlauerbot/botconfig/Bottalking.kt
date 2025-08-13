package de.dagadeta.schlauerbot.botconfig

import de.dagadeta.schlauerbot.config.BottalkingConfig
import de.dagadeta.schlauerbot.discord.SubCommandGroupProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import org.springframework.stereotype.Component

private const val CHANNEL_ID_SUBCOMMAND_NAME = "channel-id"
private const val CHANNEL_ID_OPTION_NAME = "id"

@Component
class Bottalking(
    private val bottalkingConfig: BottalkingConfig,
) : SubCommandGroupProvider {
    private val kLogger = KotlinLogging.logger {}
    override val group = "bottalking"

    var channelId = bottalkingConfig.channelId

    override fun getConfigureSubCommandGroup(): SubcommandGroupData {
        val bottalkingGroup = SubcommandGroupData(group, "Configure general bottalking settings")
        bottalkingGroup.addSubcommands(
            SubcommandData(CHANNEL_ID_SUBCOMMAND_NAME, "Sets the general bottalking channel ID (default: ${bottalkingConfig.channelId})")
                .addOption(OptionType.STRING, CHANNEL_ID_OPTION_NAME, "The channel ID", true),
        )
        return bottalkingGroup
    }

    override fun onConfigureEvent(event: SlashCommandInteractionEvent) {
        event.deferReply().queue()
        val message = when (event.interaction.subcommandName) {
            CHANNEL_ID_SUBCOMMAND_NAME -> {
                channelId = event.getOption(CHANNEL_ID_OPTION_NAME)?.asString ?: channelId
                "Channel ID set to '$channelId'."
            }
            else -> "Unknown subcommand '${event.interaction.subcommandName}'"
        }
        kLogger.info { message }
        event.hook.sendMessage(message).queue()
    }
}
