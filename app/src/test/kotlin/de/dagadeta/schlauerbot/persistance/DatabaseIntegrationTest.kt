package de.dagadeta.schlauerbot.persistance

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
@AutoConfigureEmbeddedDatabase
class DatabaseIntegrationTest() {

    @Autowired
    lateinit var gameStateRepo: WordChainGameStateRepository

    @Test
    fun `on empty database nothing is found`() {
        assertThat(gameStateRepo.findAll()).isEmpty()
    }

    @Test
    fun `the WordChainGameState can be upserted`() {
        val first = gameStateRepo.upsert(WordChainGameState(0, false, ""))
        assertThat(first).isEqualTo(WordChainGameState(0, false, ""))

        val second = gameStateRepo.upsert(WordChainGameState(0, true, "user-1"))
        assertThat(second).isEqualTo(WordChainGameState(0, true, "user-1"))

        assertThat(gameStateRepo.findAll())
            .hasSize(1)
            .contains(WordChainGameState(0, true, "user-1"))
    }
}
