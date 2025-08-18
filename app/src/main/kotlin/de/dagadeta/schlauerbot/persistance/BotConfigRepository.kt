package de.dagadeta.schlauerbot.persistance

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

interface BotConfigRepository: JpaRepository<BotConfig, ConfigId>

@Component
class BotConfigPersistenceService(val repo: BotConfigRepository) {
    @Transactional
    fun upsert(config: BotConfig): BotConfig {
        val id = ConfigId(config.commandGroup, config.name)
        val toSave = repo.findByIdOrNull(id)?.apply {
            value = config.value
            timestamp = config.timestamp
        } ?: config
        return repo.save(toSave)
    }

    fun findByIdOrNull(id: ConfigId) = repo.findByIdOrNull(id)
}
