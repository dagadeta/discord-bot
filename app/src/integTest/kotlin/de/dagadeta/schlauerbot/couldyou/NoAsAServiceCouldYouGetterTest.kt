package de.dagadeta.schlauerbot.couldyou

import de.dagadeta.schlauerbot.config.LoggingConfig
import de.dagadeta.schlauerbot.discord.Logging
import de.dagadeta.schlauerbot.externalservices.CouldYouGetter
import org.junit.jupiter.api.Test

class NoAsAServiceCouldYouGetterTest {

    private val getter = CouldYouGetter(Logging(null, LoggingConfig(0, "0")))

    @Test
    fun `a rejection reason is returned`() {
        val rejectionReason = getter.getAnswer()
        assert(rejectionReason.isNotBlank())
    }
}