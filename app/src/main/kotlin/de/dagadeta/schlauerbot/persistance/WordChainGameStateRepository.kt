package de.dagadeta.schlauerbot.persistance

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull

interface WordChainGameStateRepository: JpaRepository<WordChainGameState, Int>

fun WordChainGameStateRepository.upsert(state: WordChainGameState): WordChainGameState {
    val toSave = findByIdOrNull(state.id)?.apply {
        started = state.started
        lastUser = state.lastUser
    } ?: state
    return save(toSave)
}

interface UsedWordRepository: JpaRepository<UsedWord, String>
