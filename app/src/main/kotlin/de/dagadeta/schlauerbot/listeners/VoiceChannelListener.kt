package de.dagadeta.schlauerbot.listeners

import de.dagadeta.schlauerbot.config.AdminConfig
import de.dagadeta.schlauerbot.discord.Logging
import de.dagadeta.schlauerbot.discord.PermissionValidator
import de.dagadeta.schlauerbot.discord.SubCommandGroupProvider
import de.dagadeta.schlauerbot.persistance.BotConfig
import de.dagadeta.schlauerbot.persistance.BotConfigPersistenceService
import de.dagadeta.schlauerbot.persistance.ConfigId
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import org.springframework.stereotype.Service
import java.lang.Thread.sleep

private const val PING_ROLE_ID_SUBCOMMAND_NAME = "ping-role-id"
private const val PING_ROLE_ID_OPTION_NAME = "id"

@Service
class VoiceChannelListener(
    private val logging: Logging,
    private val api: JDA,
    private val botConfigRepo: BotConfigPersistenceService,
    private val adminConfig: AdminConfig,
    private val permissionValidator: PermissionValidator,
) : ListenerAdapter(), SubCommandGroupProvider {
    override val group = "voice-channel-listener"
    private val kLogger = KotlinLogging.logger {}
    private var voiceChannelPingRoleId = botConfigRepo.findByIdOrNull(ConfigId(group, PING_ROLE_ID_SUBCOMMAND_NAME))?.value ?: ""

    @PostConstruct
    fun startListener() {
        api.addEventListener(this)

        logging.log("${VoiceChannelListener::class.simpleName} started.")
        if (voiceChannelPingRoleId.isEmpty()) {
            logging.log("WARNING: The voice channel ping role ID is not yet configured. Use the `/config`-command to set it.")
        }
    }

    @PreDestroy
    fun stopListener() {
        api.removeEventListener(this)
        logging.log("${VoiceChannelListener::class.simpleName} stopped.")
        sleep(2000) // give the asynchronous tasks time to finish before cutting the connection
    }

    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        val joinedChannel = event.channelJoined ?: return
        if (event.member.user.isBot) return

        if (joinedChannel is VoiceChannel && joinedChannel.members.size == 1) {
            kLogger.info { "User ${event.member.effectiveName} joined voice channel ${joinedChannel.name}" }
            joinedChannel.sendMessage("<@&$voiceChannelPingRoleId> **${event.member.effectiveName}** just started a voice call!").queue()
        }
    }

    override fun getConfigureSubCommandGroup(): SubcommandGroupData {
        val voiceChannelListenerGroup = SubcommandGroupData(group, "Configure the voice channel listener")
        voiceChannelListenerGroup.addSubcommands(
            SubcommandData(PING_ROLE_ID_SUBCOMMAND_NAME, "Sets the voice channel ping role ID")
                .addOption(OptionType.STRING, PING_ROLE_ID_OPTION_NAME, "The role ID", true),
        )
        return voiceChannelListenerGroup
    }

    override fun onConfigureEvent(event: SlashCommandInteractionEvent) {
        if (!permissionValidator.checkAdminAccess(event, adminConfig)) return

        event.deferReply().queue()
        val message = when (event.interaction.subcommandName) {
            PING_ROLE_ID_SUBCOMMAND_NAME -> {
                voiceChannelPingRoleId = event.getOption(PING_ROLE_ID_OPTION_NAME)?.asString ?: voiceChannelPingRoleId
                botConfigRepo.upsert(BotConfig(group, PING_ROLE_ID_SUBCOMMAND_NAME, voiceChannelPingRoleId))
                "Voice channel ping role ID set to '$voiceChannelPingRoleId'."
            }
            else -> "Unknown subcommand '${event.interaction.subcommandName}'"
        }
        kLogger.info { message }
        event.hook.sendMessage(message).queue()
    }
}
