package de.dagadeta.schlauerbot.countinggame

import de.dagadeta.schlauerbot.common.FailureType
import de.dagadeta.schlauerbot.common.FailureType.Critical
import de.dagadeta.schlauerbot.common.FailureType.Unspectacular
import de.dagadeta.schlauerbot.common.onFailure
import de.dagadeta.schlauerbot.common.onSuccess
import de.dagadeta.schlauerbot.common.sendPrivateMessage
import de.dagadeta.schlauerbot.config.AdminConfig
import de.dagadeta.schlauerbot.countinggame.CountingGame.CanNotCountFlag.RESET
import de.dagadeta.schlauerbot.discord.Logging
import de.dagadeta.schlauerbot.discord.PermissionValidator
import de.dagadeta.schlauerbot.discord.SubCommandGroupProvider
import de.dagadeta.schlauerbot.persistance.BotConfig
import de.dagadeta.schlauerbot.persistance.BotConfigPersistenceService
import de.dagadeta.schlauerbot.persistance.ConfigId
import de.dagadeta.schlauerbot.persistance.CountingGameStatePersistenceService
import de.dagadeta.schlauerbot.persistance.UserStatePersistenceService
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

private const val CHANNEL_ID_SUBCOMMAND_NAME = "channel-id"
private const val CHANNEL_ID_OPTION_NAME = "id"
private const val CAN_NOT_COUNT_ROLE_ID_SUBCOMMAND_NAME = "can-not-count-role-id"
private const val CAN_NOT_COUNT_ROLE_ID_OPTION_NAME = "id"
private const val CAN_NOT_COUNT_RESET_THRESHOLD_SUBCOMMAND_NAME = "can-not-count-reset-threshold"
private const val CAN_NOT_COUNT_RESET_THRESHOLD_OPTION_NAME = "streak"

@Service
class DiscordCountingGame(
    private val logging: Logging,
    private val api: JDA,
    gameStateRepo: CountingGameStatePersistenceService,
    userStateRepo: UserStatePersistenceService,
    private val botConfigRepo: BotConfigPersistenceService,
    private val adminConfig: AdminConfig,
    private val permissionValidator: PermissionValidator,
) : ListenerAdapter(), SubCommandGroupProvider {
    override val group = "counting-game"
    private val kLogger = KotlinLogging.logger {}
    private val allCommandNames = CountingGameCommand.entries.map(CountingGameCommand::command)
    private var channelId = botConfigRepo.findByIdOrNull(ConfigId(group, CHANNEL_ID_SUBCOMMAND_NAME))?.value ?: ""
    private var canNotCountRoleId = botConfigRepo.findByIdOrNull(ConfigId(group, CAN_NOT_COUNT_ROLE_ID_SUBCOMMAND_NAME))?.value ?: ""
    private val defaultCanNotCountThreshold = 10
    private var canNotCountResetThreshold = botConfigRepo.findByIdOrNull(ConfigId(group, CAN_NOT_COUNT_RESET_THRESHOLD_SUBCOMMAND_NAME))?.value?.toIntOrNull() ?: defaultCanNotCountThreshold
    private val game: CountingGame = CountingGame(gameStateRepo, userStateRepo, canNotCountResetThreshold)

    @PostConstruct
    fun startListener() {
        api.addEventListener(this)
        CountingGameCommand.entries.forEach {
            api.upsertCommand(it.command, it.description).queue()
        }
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

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name !in allCommandNames) return

        event.deferReply().queue()
        val message = when (event.name) {
            CountingGameCommand.Streak.command -> game.generateStreakMessage(event.user.id)
            else -> "Unknown command '${event.name}'"
        }
        event.hook.sendMessage(message).queue()
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channel.id != channelId || event.author.isBot) return
        val parsedMessage = parseMessage(event.message.contentDisplay) ?: return

        game.onMessageReceived(event.author.id, parsedMessage)
            .onSuccess { canNotCountFlag ->
                event.message.addReaction(Emoji.fromUnicode("🐸")).queue()
                if (canNotCountFlag == RESET) {
                    event.guild.getRoleById(canNotCountRoleId)?.let {
                        event.guild.removeRoleFromMember(event.author, it).queue()
                    }
                    event.message.reply("At least for now it seems like you can count again! Phew.").queue()
                }
            }
            .onFailure {
                message, type -> onInvalidMessage(event, message, type)
                event.guild.getRoleById(canNotCountRoleId)?.let {
                    event.guild.addRoleToMember(event.author, it).queue()
                }
            }
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
            Unspectacular -> sendPrivateMessage(event.message, replyMessage)
            Critical -> {
                event.message.reply(replyMessage).queue()
                event.message.addReaction(Emoji.fromUnicode("💥")).queue()
            }
        }
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
            SubcommandData(CAN_NOT_COUNT_RESET_THRESHOLD_SUBCOMMAND_NAME, "Sets the counting streak at which the can-not-count role is removed (default: $defaultCanNotCountThreshold)")
                .addOption(OptionType.INTEGER, CAN_NOT_COUNT_RESET_THRESHOLD_OPTION_NAME, "The required streak", true),
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
                canNotCountRoleId = event.getOption(CAN_NOT_COUNT_ROLE_ID_OPTION_NAME)?.asString ?: ""
                botConfigRepo.upsert(BotConfig(group, CAN_NOT_COUNT_ROLE_ID_SUBCOMMAND_NAME, canNotCountRoleId))
                "Can not count role ID set to '$canNotCountRoleId'."
            }
            CAN_NOT_COUNT_RESET_THRESHOLD_SUBCOMMAND_NAME -> {
                canNotCountResetThreshold = event.getOption(CAN_NOT_COUNT_RESET_THRESHOLD_OPTION_NAME)?.asInt ?: canNotCountResetThreshold
                botConfigRepo.upsert(BotConfig(group, CAN_NOT_COUNT_RESET_THRESHOLD_SUBCOMMAND_NAME, canNotCountResetThreshold.toString()))
                "Can not count reset threshold set to '$canNotCountResetThreshold'."
            }
            else -> "Unknown subcommand '${event.interaction.subcommandName}'"
        }
        kLogger.info { message }
        event.hook.sendMessage(message).queue()
    }
}
