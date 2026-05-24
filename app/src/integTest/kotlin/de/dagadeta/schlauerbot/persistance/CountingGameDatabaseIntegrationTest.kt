package de.dagadeta.schlauerbot.persistance

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("integTest")
@AutoConfigureEmbeddedDatabase
class CountingGameDatabaseIntegrationTest {

    @Autowired
    lateinit var gameStateRepo: CountingGameStateRepository

    @Test
    fun `on empty database nothing is found`() {
        assertThat(gameStateRepo.findAll()).isEmpty()
    }

    @Test
    fun `a saved gameState can be read`() {
        gameStateRepo.save(CountingGameState(0, 7, "Vader"))
        val persistenceService = CountingGameStatePersistenceService(gameStateRepo)

        assertThat(persistenceService.findByIdOrNull(0)).isEqualTo(CountingGameState(0, 7, "Vader"))
    }

    @Test
    fun `the CountingGameState can be upserted`() {
        val persistenceService = CountingGameStatePersistenceService(gameStateRepo)
        val first = persistenceService.upsert(CountingGameState(0, 4, "Vader"))
        assertThat(first).isEqualTo(CountingGameState(0, 4, "Vader"))
        assertThat(gameStateRepo.findAll().single()).isEqualTo(first)

        val second = persistenceService.upsert(CountingGameState(0, 896, "Yoda"))
        assertThat(second).isEqualTo(CountingGameState(0, 896, "Yoda"))

        assertThat(gameStateRepo.findAll().single()).isEqualTo(second)
        assertThat(gameStateRepo.findAll())
            .hasSize(1)
            .contains(CountingGameState(0, 896, "Yoda"))
    }
}
