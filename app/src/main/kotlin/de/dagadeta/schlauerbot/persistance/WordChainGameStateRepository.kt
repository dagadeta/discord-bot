package de.dagadeta.schlauerbot.persistance

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

interface WordChainGameStateRepository: JpaRepository<WordChainGameState, Int>

@Component
class WordChainGameStatePersistenceService(val repo: WordChainGameStateRepository) {
    @Transactional
    fun upsert(state: WordChainGameState): WordChainGameState {
        val toSave = repo.findByIdOrNull(state.id)?.apply {
            started = state.started
            lastUser = state.lastUser
        } ?: state
        return repo.save(toSave)
    }

    fun findByIdOrNull(id: Int) = repo.findByIdOrNull(id)
}

interface UsedWordRepository: JpaRepository<UsedWord, String>
