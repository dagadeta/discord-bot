package de.dagadeta.schlauerbot.countinggame

import de.dagadeta.schlauerbot.common.FailureType
import de.dagadeta.schlauerbot.common.onFailure
import de.dagadeta.schlauerbot.common.onSuccess
import de.dagadeta.schlauerbot.config.AdminConfig
import de.dagadeta.schlauerbot.discord.Logging
import de.dagadeta.schlauerbot.discord.PermissionValidator
import de.dagadeta.schlauerbot.discord.SubCommandGroupProvider
import de.dagadeta.schlauerbot.persistance.BotConfig
import de.dagadeta.schlauerbot.persistance.BotConfigPersistenceService
import de.dagadeta.schlauerbot.persistance.ConfigId
import de.dagadeta.schlauerbot.persistance.CountingGameStatePersistenceService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import org.mariuszgromada.math.mxparser.Expression
import org.springframework.stereotype.Service
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit

private const val CHANNEL_ID_SUBCOMMAND_NAME = "channel-id"
private const val CHANNEL_ID_OPTION_NAME = "id"
private const val CAN_NOT_COUNT_ROLE_ID_SUBCOMMAND_NAME = "can-not-count-role-id"
private const val CAN_NOT_COUNT_ROLE_ID_OPTION_NAME = "id"

@Service
class DiscordCountingGame(
    private val logging: Logging,
    private val api: JDA,
    gameStateRepo: CountingGameStatePersistenceService,
    private val botConfigRepo: BotConfigPersistenceService,
    private val adminConfig: AdminConfig,
    private val permissionValidator: PermissionValidator,
) : ListenerAdapter(), SubCommandGroupProvider {
    override val group = "counting-game"
    private val kLogger = KotlinLogging.logger {}
    private val game: CountingGame = CountingGame(gameStateRepo)
    private var channelId = botConfigRepo.findByIdOrNull(ConfigId(group, CHANNEL_ID_SUBCOMMAND_NAME))?.value ?: ""

    @PostConstruct
    fun startListener() {
        api.addEventListener(this)
        writeInitialStateTo(logging)

        logging.log("${DiscordCountingGame::class.simpleName} started.")
        if (channelId.isEmpty()) {
            logging.log("WARNING: The counting game channel ID is not yet configured. Use the `/config`-command to set it.")
        }
    }

    @PreDestroy
    fun stopListener() {
        api.removeEventListener(this)
        logging.log("${DiscordCountingGame::class.simpleName} stopped.")
        sleep(2000) // give the asynchronous tasks time to finish before cutting the connection
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channel.id != channelId || event.author.isBot) return
        val parsedMessage = parseMessage(event.message.contentDisplay) ?: return

        game.onMessageReceived(event.author.id, parsedMessage)
            .onSuccess { event.message.addReaction(Emoji.fromUnicode("🐸")).queue() }
            .onFailure { message, type -> onInvalidMessage(event, message, type) }
    }

    private fun parseMessage(message: String): Int? {
        val directInt = message.toIntOrNull()
        if (directInt != null) return directInt

        val e = Expression(message)
        if (!e.checkSyntax()) return null
        val result = e.calculate()

        return if (!result.isNaN() && result % 1.0 == 0.0) {
            result.toInt()
        } else {
            null
        }
    }

    fun onInvalidMessage(event: MessageReceivedEvent, replyMessage: String, failureType: FailureType) {
        when (failureType) {
            FailureType.Unspectacular -> sendWarningMessage(event, replyMessage)
            FailureType.Critical -> {
                event.message.reply(replyMessage).queue()
                event.message.addReaction(Emoji.fromUnicode("💥")).queue()
            }
        }
    }

    fun sendWarningMessage(event: MessageReceivedEvent, replyMessage: String) {
        fun temporaryReplyFallback() {
            event.message.reply(replyMessage).queue { reply ->
                event.message.delete().queueAfter(3, TimeUnit.SECONDS)
                reply.delete().queueAfter(3, TimeUnit.SECONDS)
            }
        }

        event.message.author.openPrivateChannel()
            .queue({ channel ->
                channel.sendMessage(replyMessage).queue(
                    { event.message.delete().queue() },
                    { temporaryReplyFallback() }
                )
            }, { temporaryReplyFallback() })
    }

    fun writeInitialStateTo(logging: Logging) {
        logging.log(game.describeInitialState())
    }

    override fun getConfigureSubCommandGroup(): SubcommandGroupData {
        val countingGameGroup = SubcommandGroupData(group, "configure the counting game")
        countingGameGroup.addSubcommands(
            SubcommandData(CHANNEL_ID_SUBCOMMAND_NAME, "Sets the counting game's channel ID")
                .addOption(OptionType.STRING, CHANNEL_ID_OPTION_NAME, "The channel ID", true),
            SubcommandData(CAN_NOT_COUNT_ROLE_ID_SUBCOMMAND_NAME, "Sets the role ID for users that can not count")
                .addOption(OptionType.STRING, CAN_NOT_COUNT_ROLE_ID_OPTION_NAME, "The role ID", true),
        )
        return countingGameGroup
    }

    override fun onConfigureEvent(event: SlashCommandInteractionEvent) {
        if (!permissionValidator.checkAdminAccess(event, adminConfig)) return

        event.deferReply().queue()
        val message = when (event.interaction.subcommandName) {
            CHANNEL_ID_SUBCOMMAND_NAME -> {
                channelId = event.getOption(CHANNEL_ID_OPTION_NAME)?.asString ?: channelId
                botConfigRepo.upsert(BotConfig(group, CHANNEL_ID_SUBCOMMAND_NAME, channelId))
                "Channel ID set to '$channelId'."
            }
            CAN_NOT_COUNT_ROLE_ID_SUBCOMMAND_NAME -> {
                val roleId = event.getOption(CAN_NOT_COUNT_ROLE_ID_OPTION_NAME)?.asString ?: ""
                botConfigRepo.upsert(BotConfig(group, CAN_NOT_COUNT_ROLE_ID_SUBCOMMAND_NAME, roleId))
                "Can not count role ID set to '$roleId'."
            }
            else -> "Unknown subcommand '${event.interaction.subcommandName}'"
        }
        kLogger.info { message }
        event.hook.sendMessage(message).queue()
    }
}
