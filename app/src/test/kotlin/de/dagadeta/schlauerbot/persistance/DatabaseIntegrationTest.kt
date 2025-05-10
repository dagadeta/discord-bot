package de.dagadeta.schlauerbot.persistance

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
// TODO: These tests fail by now. Here are some ideas to fix them:
//@SpringBootTest
//@ContextConfiguration(classes = [WordChainGameStateRepository::class, WordChainGameStatePersistenceService::class])
@AutoConfigureEmbeddedDatabase
class DatabaseIntegrationTest() {

    @Autowired
    lateinit var gameStateRepo: WordChainGameStateRepository

    @Autowired
    lateinit var persistenceService: WordChainGameStatePersistenceService

    @Test
    fun `on empty database nothing is found`() {
        assertThat(gameStateRepo.findAll()).isEmpty()
    }

    @Test
    fun `the WordChainGameState can be upserted`() {
        val first = persistenceService.upsert(WordChainGameState(0, false, ""))
        assertThat(first).isEqualTo(WordChainGameState(0, false, ""))
        assertThat(gameStateRepo.findAll().single()).isEqualTo(first)

        val second = persistenceService.upsert(WordChainGameState(0, true, "user-1"))
        assertThat(second).isEqualTo(WordChainGameState(0, true, "user-1"))

        assertThat(gameStateRepo.findAll().single()).isEqualTo(second)
        assertThat(gameStateRepo.findAll())
            .hasSize(1)
            .contains(WordChainGameState(0, true, "user-1"))
    }
}
