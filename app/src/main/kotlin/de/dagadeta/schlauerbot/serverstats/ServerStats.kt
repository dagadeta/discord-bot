package de.dagadeta.schlauerbot.serverstats

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
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.ChannelType.VOICE
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import org.springframework.stereotype.Service
import java.lang.Thread.sleep

private const val CHANNEL_ID_SUBCOMMAND_NAME = "channel-id"
private const val CHANNEL_ID_OPTION_NAME = "id"

@Service
class ServerStats(
    private val logging: Logging,
    private val api: JDA,
    private val botConfigRepo: BotConfigPersistenceService,
    private val adminConfig: AdminConfig,
    private val permissionValidator: PermissionValidator,
) : ListenerAdapter(), SubCommandGroupProvider {
    override val group = "server-stats"
    private val kLogger = KotlinLogging.logger {}
    private var channelId = botConfigRepo.findByIdOrNull(ConfigId(group, CHANNEL_ID_SUBCOMMAND_NAME))?.value ?: ""

    @PostConstruct
    fun startListener() {
        api.addEventListener(this)

        logging.log("${ServerStats::class.simpleName} started.")
        if (channelId.isEmpty()) {
            logging.log("WARNING: The server stats channel ID is not yet configured. Use the `/config`-command to set it.")
        }

        setServerStats()
    }

    @PreDestroy
    fun stopListener() {
        api.removeEventListener(this)
        logging.log("${ServerStats::class.simpleName} stopped.")
        sleep(2000) // give the asynchronous tasks time to finish before cutting the connection
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) { setServerStats() }
    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) { setServerStats() }

    private fun setServerStats() {
        if (channelId.isEmpty()) return

        val channel = api.getVoiceChannelById(channelId)
        if (channel == null) {
            logging.log("WARNING: The set server stats channel ID is not valid.")
            return
        }



        channel.guild.loadMembers().onSuccess { members ->
            val userCount = countUsers(members)
            val title = "👥・${userCount.humanCount}｜🤖・${userCount.botCount}"

            channel.manager.setName(title).queue()
            logging.log("Server stats updated: '$title'")
        }.onError { error ->
            kLogger.error(error) { "Failed to load members for server stats: ${error.message}" }
            logging.log("ERROR: Failed to load server members for server stats: ${error.message}")
        }
    }

    internal fun countUsers(members: List<Member>): UserCount {
        var humanCount = 0
        var botCount = 0

        for (member in members) {
            if (member.user.isBot) botCount++ else humanCount++
        }

        return UserCount(humanCount = humanCount, botCount = botCount)
    }

    override fun getConfigureSubCommandGroup(): SubcommandGroupData {
        val serverStatsGroup = SubcommandGroupData(group, "configure the server stats channel")
        serverStatsGroup.addSubcommands(
            SubcommandData(CHANNEL_ID_SUBCOMMAND_NAME, "Sets the server stats channel ID (needs to be a voice channel)")
                .addOption(OptionType.STRING, CHANNEL_ID_OPTION_NAME, "The channel ID", true),
        )
        return serverStatsGroup
    }

    override fun onConfigureEvent(event: SlashCommandInteractionEvent) {
        if (!permissionValidator.checkAdminAccess(event, adminConfig)) return

        event.deferReply().queue()
        val message = when (event.interaction.subcommandName) {
            CHANNEL_ID_SUBCOMMAND_NAME -> {
                channelId = event.getOption(CHANNEL_ID_OPTION_NAME)?.asString ?: channelId
                if (api.getGuildChannelById(channelId)?.type == VOICE) {
                    botConfigRepo.upsert(BotConfig(group, CHANNEL_ID_SUBCOMMAND_NAME, channelId))
                    setServerStats()
                    "Channel ID set to '$channelId'. Server stats are being updated."
                } else {
                    "The Channel ID is either not valid or not a voice channel."
                }
            }
            else -> "Unknown subcommand '${event.interaction.subcommandName}'"
        }
        kLogger.info { message }
        event.hook.sendMessage(message).queue()
    }

    data class UserCount(val humanCount: Int, val botCount: Int)
}
