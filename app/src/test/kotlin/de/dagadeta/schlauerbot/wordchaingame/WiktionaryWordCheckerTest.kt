package de.dagadeta.schlauerbot.wordchaingame

import de.dagadeta.schlauerbot.config.LoggingConfig
import de.dagadeta.schlauerbot.discord.Logging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WiktionaryWordCheckerTest {

    private val wordChecker = WiktionaryWordChecker("en", Logging(null, LoggingConfig(0, 0)))

    @Test
    fun `a word can be checked`() {
        assertThat(wordChecker.isValidWord("house")).isTrue
        assertThat(wordChecker.isValidWord("slitherfang")).isFalse
    }
}
