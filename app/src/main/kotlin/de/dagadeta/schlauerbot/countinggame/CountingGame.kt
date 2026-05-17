package de.dagadeta.schlauerbot.countinggame

import de.dagadeta.schlauerbot.common.FailureType
import de.dagadeta.schlauerbot.common.Result
import de.dagadeta.schlauerbot.common.Result.Companion.failure
import de.dagadeta.schlauerbot.common.Result.Companion.success
import de.dagadeta.schlauerbot.persistance.CountingGameState
import de.dagadeta.schlauerbot.persistance.CountingGameStatePersistenceService
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class CountingGame(
    private var gameStateRepo: CountingGameStatePersistenceService,
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

    fun onMessageReceived(userId: String, number: Int): Result<Unit> = when {
        lastUserId == userId -> {
            failure("You're not alone here! Let the others write numbers too!")
        }
        number != currentNumber + 1 -> {
            when (currentNumber) {
                startingNumber - 1 -> {
                    resetGame()
                    failure("You didn't even manage to write the first number! Let's try that again (Tip: It's $startingNumber...)")
                }
                else -> {
                    val ruinedNumber = currentNumber
                    resetGame()
                    failure("You RUINED it at $ruinedNumber! Let's start over with $startingNumber...", FailureType.Critical)
                }
            }
        }
        else -> {
            logger.info { "received CountingGame number $number" }
            lastUserId = userId
            currentNumber = number

            saveState()

            success(Unit)
        }
    }

    fun resetGame() {
        currentNumber = startingNumber - 1
        lastUserId = ""
        saveState()
    }

    fun describeInitialState(): String = "Resuming CountingGame at $currentNumber."

    private fun saveState() = gameStateRepo.upsert(CountingGameState(theGameId, currentNumber, lastUserId))
}
