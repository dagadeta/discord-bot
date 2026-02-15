package de.dagadeta.schlauerbot.externalservices

import de.dagadeta.schlauerbot.config.LoggingConfig
import de.dagadeta.schlauerbot.discord.Logging
import org.junit.jupiter.api.Test

class HttpGetterTest {
    private val logger = Logging(null, LoggingConfig(0, "0"))

    @Test
    fun `a rejection reason is returned`() {
        val getter = CouldYouGetter(logger)
        val rejectionReason = getter.getAnswer()
        assert(rejectionReason.isNotBlank())
    }

    @Test
    fun `a random useless fact is returned`() {
        val getter = RandomUselessFactGetter(logger)
        val fact = getter.getAnswer()
        assert(fact.isNotBlank())
    }
}
