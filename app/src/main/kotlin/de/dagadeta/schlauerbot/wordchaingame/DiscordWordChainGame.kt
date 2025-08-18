package de.dagadeta.schlauerbot.wordchaingame

import de.dagadeta.schlauerbot.common.onFailure
import de.dagadeta.schlauerbot.discord.Logging
import de.dagadeta.schlauerbot.discord.SubCommandGroupProvider
import de.dagadeta.schlauerbot.persistance.*
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import org.springframework.stereotype.Service
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit

private const val CHANNEL_ID_SUBCOMMAND_NAME = "channel-id"
private const val CHANNEL_ID_OPTION_NAME = "id"
private const val LANGUAGE = "language"
private const val CHECK_WORD_EXISTENCE_SUBCOMMAND_NAME = "check-word-existence"
private const val CHECK_WORD_EXISTENCE_OPTION_NAME = "check"

private const val DEFAULT_LANGUAGE = "en"
private const val DEFAULT_CHECK_WORD_EXISTENCE = true

@Service
class DiscordWordChainGame(
    private val logging: Logging,
    private val api: JDA,
    gameStateRepo: WordChainGameStatePersistenceService,
    usedWordRepo: UsedWordRepository,
    private val botConfigRepo: BotConfigPersistenceService,
) : ListenerAdapter(), SubCommandGroupProvider {
    override val group = "word-chain-game"
    private val kLogger = KotlinLogging.logger {}
    private val allCommandNames = WordChainGameCommand.entries.map(WordChainGameCommand::command)
    private val game: WordChainGame
    private var channelId = botConfigRepo.findByIdOrNull(ConfigId(group, CHANNEL_ID_SUBCOMMAND_NAME))?.value ?: ""

    init {
        val language = botConfigRepo.findByIdOrNull(ConfigId(group, LANGUAGE))?.value ?: DEFAULT_LANGUAGE
        game = WordChainGame(
            language,
            WiktionaryWordChecker(language, logging),
            gameStateRepo,
            usedWordRepo,
            botConfigRepo.findByIdOrNull(ConfigId(group, CHECK_WORD_EXISTENCE_SUBCOMMAND_NAME))?.value?.toBoolean() ?: DEFAULT_CHECK_WORD_EXISTENCE
        )
    }

    @PostConstruct
    fun startListener() {
        api.addEventListener(this)
        WordChainGameCommand.entries.forEach {
            api.upsertCommand(Commands.slash(it.command, it.description)).queue()
        }
        writeInitialStateTo(logging)

        logging.log("${DiscordWordChainGame::class.simpleName} started (language=${game.language}, checkWordExistence=${game.checkWordExistence}).")
        if (channelId.isEmpty()) {
            logging.log("WARNING: The word chain game channel ID is not yet configured. Use the `/config`-command to set it.")
        }
    }

    @PreDestroy
    fun stopListener() {
        api.removeEventListener(this)
        logging.log("${DiscordWordChainGame::class.simpleName} stopped.")
        sleep(2000) // give the asynchronous tasks time to finish before cutting the connection
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        // don't react on unknown commands; there might be others that are not word chain game related
        if (event.name !in allCommandNames) return

        event.deferReply().queue()
        val message = when (event.name) {
            WordChainGameCommand.Start.command -> game.startGame()
            WordChainGameCommand.Stop.command -> game.stopGame()
            WordChainGameCommand.Pause.command -> game.pauseGame()
            WordChainGameCommand.Restart.command -> game.restartGame()
            else -> "Unknown command '${event.name}'"
        }
        event.hook.sendMessage(message).queue()
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channel.id != channelId || event.author.isBot) return
        game.onMessageReceived(event.author.id, event.message.contentDisplay)
            .onFailure { answer -> sendInvalidWordMessage(event.message, answer) }
    }

    private fun sendInvalidWordMessage(originalMessage: Message, replyMessage: String) {
        fun temporaryReplyFallback() {
            originalMessage.reply(replyMessage).queue { reply ->
                originalMessage.delete().queueAfter(3, TimeUnit.SECONDS)
                reply.delete().queueAfter(3, TimeUnit.SECONDS)
            }
        }

        originalMessage.author.openPrivateChannel()
            .queue({ channel ->
                channel.sendMessage(replyMessage).queue(
                    { _ -> originalMessage.delete().queue() },
                    { _ -> temporaryReplyFallback() }
                )
            }, { _ ->
                temporaryReplyFallback()
            })
    }

    fun writeInitialStateTo(logging: Logging) {
        logging.log(game.describeInitialState())
    }

    override fun getConfigureSubCommandGroup(): SubcommandGroupData {
        val wordChainGameGroup = SubcommandGroupData(group, "configure the WordChain game")
        wordChainGameGroup.addSubcommands(
            SubcommandData(CHANNEL_ID_SUBCOMMAND_NAME, "Sets the WordChain game's channel ID")
                .addOption(OptionType.STRING, CHANNEL_ID_OPTION_NAME, "The channel ID", true),
            SubcommandData(LANGUAGE, "Sets the WordChain game's language (default: $DEFAULT_LANGUAGE)")
                .addOption(OptionType.STRING, LANGUAGE, "e.g. 'en' or 'de'", true),
            SubcommandData(CHECK_WORD_EXISTENCE_SUBCOMMAND_NAME, "Configure if the WordChain game should check if words exist in the dictionary (default: $DEFAULT_CHECK_WORD_EXISTENCE)")
                .addOption(OptionType.BOOLEAN, CHECK_WORD_EXISTENCE_OPTION_NAME, "true or false", true),
        )
        return wordChainGameGroup
    }

    override fun onConfigureEvent(event: SlashCommandInteractionEvent) {
        event.deferReply().queue()
        val message = when (event.interaction.subcommandName) {
            CHANNEL_ID_SUBCOMMAND_NAME -> {
                channelId = event.getOption(CHANNEL_ID_OPTION_NAME)?.asString ?: channelId
                botConfigRepo.upsert(BotConfig(group, CHANNEL_ID_SUBCOMMAND_NAME, channelId))
                "Channel ID set to '$channelId'."
            }
            LANGUAGE -> {
                val language = event.getOption(LANGUAGE)?.asString ?: game.language
                game.setLanguage(language, WiktionaryWordChecker(language, logging))
                botConfigRepo.upsert(BotConfig(group, LANGUAGE, language))
                "Language set to '$language'."
            }
            CHECK_WORD_EXISTENCE_SUBCOMMAND_NAME -> {
                game.checkWordExistence = event.getOption(CHECK_WORD_EXISTENCE_OPTION_NAME)?.asBoolean ?: game.checkWordExistence
                botConfigRepo.upsert(BotConfig(group, CHECK_WORD_EXISTENCE_SUBCOMMAND_NAME, game.checkWordExistence.toString()))
                "Word existence check set to '${game.checkWordExistence}'."
            }
            else -> "Unknown subcommand '${event.interaction.subcommandName}'"
        }
        kLogger.info { message }
        event.hook.sendMessage(message).queue()
    }
}
