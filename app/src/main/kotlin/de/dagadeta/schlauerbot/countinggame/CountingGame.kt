package de.dagadeta.schlauerbot.countinggame

import de.dagadeta.schlauerbot.common.FailureType
import de.dagadeta.schlauerbot.common.Result
import de.dagadeta.schlauerbot.common.Result.Companion.failure
import de.dagadeta.schlauerbot.common.Result.Companion.success
import de.dagadeta.schlauerbot.persistance.CountingGameState
import de.dagadeta.schlauerbot.persistance.CountingGameStatePersistenceService
import de.dagadeta.schlauerbot.persistance.UserState
import de.dagadeta.schlauerbot.persistance.UserStatePersistenceService
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class CountingGame(
    private val gameStateRepo: CountingGameStatePersistenceService,
    private val userStateRepo: UserStatePersistenceService,
    private val canNotCountResetThreshold: Int,
) {
    private val theGameId = 0

    private val startingNumber = 0
    private var currentNumber: Int = startingNumber - 1
    private var lastUserId: String = ""

    init {
        gameStateRepo.findByIdOrNull(theGameId)?.let {
            currentNumber = it.count
            lastUserId = it.lastUser
        }
    }

    fun onMessageReceived(userId: String, number: Int): Result<CanNotCountFlag> {
        val userState = userStateRepo.findByIdOrNull(userId) ?: UserState(userId, 0, 0, false, 0)

        return when {
            lastUserId == userId -> {
                failure("You're not alone here! Let the others write numbers too!")
            }

            number != currentNumber + 1 -> {
                when (currentNumber) {
                    startingNumber - 1 -> {
                        resetGame()
                        saveCountingFailed(userState)
                        failure("You didn't even manage to write the first number! Let's try that again (Tip: It's $startingNumber...)")
                    }

                    else -> {
                        val ruinedNumber = currentNumber
                        resetGame()
                        saveCountingFailed(userState)
                        failure(
                            "You RUINED it at $ruinedNumber! Let's start over with $startingNumber...",
                            FailureType.Critical
                        )
                    }
                }
            }

            else -> {
                logger.info { "received CountingGame number $number" }
                lastUserId = userId
                currentNumber = number

                val canNotCountFlag = saveCountingSucceeded(userState)
                saveState()

                success(canNotCountFlag)
            }
        }
    }

    private fun saveCountingSucceeded(userState: UserState): CanNotCountFlag {
        userState.streak++
        userState.correctNumbersAmount++
        userState.longestStreak = maxOf(userState.longestStreak, userState.streak)
        val canNotCountFlag = if (userState.streak >= canNotCountResetThreshold && userState.canNotCount) {
            userState.canNotCount = false
            CanNotCountFlag.RESET
        } else {
            CanNotCountFlag.UNCHANGED
        }
        userStateRepo.upsert(userState)
        return canNotCountFlag
    }

    private fun saveCountingFailed(userState: UserState) {
        userState.streak = 0
        userState.canNotCount = true
        userStateRepo.upsert(userState)
    }

    fun resetGame() {
        currentNumber = startingNumber - 1
        lastUserId = ""
        saveState()
    }

    fun describeInitialState(): String = "Resuming CountingGame at $currentNumber."

    fun generateStatsMessage(userId: String): String {
        val state = userStateRepo.findByIdOrNull(userId)
        return if (state?.longestStreak == null) {
            "You haven't counted yet! 🥲"
        } else {
            """
                Streak: ${state.streak}
                Longest Streak: ${state.longestStreak}
                Total Correct Numbers: ${state.correctNumbersAmount}
            """.trimIndent()
        }
    }

    fun generateLeaderboardMessage(userId: String, leaderboards: List<CountingGameLeaderboard>): String {
        val allUsers = userStateRepo.findAll()

        fun CountingGameLeaderboard.formatLeaderboard(): String {
            val sortedUsers = allUsers.sortedByDescending(selector)
            val bestUsers = sortedUsers.take(limit)

            val builder = StringBuilder("**${title}**\n")
            bestUsers.forEachIndexed { index, userState ->
                builder.append("${index + 1}. <@${userState.userId}> | ${selector(userState)}\n")
            }

            if (bestUsers.none { it.userId == userId }) {
                val userIndex = sortedUsers.indexOfFirst { it.userId == userId }
                if (userIndex != -1) {
                    builder.append("...\n")
                    builder.append("${userIndex + 1}. <@$userId> | ${selector(sortedUsers[userIndex])}\n")
                }
            }

            return builder.toString()
        }

        return leaderboards.joinToString("\n") { it.formatLeaderboard() }
    }

    private fun saveState() = gameStateRepo.upsert(CountingGameState(theGameId, currentNumber, lastUserId))

    enum class CanNotCountFlag {
        RESET, UNCHANGED
    }

    enum class CountingGameLeaderboard(
        val title: String,
        val selector: (UserState) -> Int,
        val limit: Int = 10,
    ) {
        CurrentStreak("Current Streaks", { it.streak }),
        LongestStreak("Longest Streaks", { it.longestStreak }),
        CorrectNumbers("Total Correct Numbers", { it.correctNumbersAmount }),
    }
}

enum class CountingGameCommand(val command: String, val description: String) {
    Stats("counting-game-stats", "Shows your counting game statistics"),
    Leaderboard("counting-game-leaderboard", "Shows a server-wide leaderboard for the counting game")
}
