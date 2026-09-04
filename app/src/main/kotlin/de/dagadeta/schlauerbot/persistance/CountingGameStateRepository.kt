package de.dagadeta.schlauerbot.persistance

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

interface CountingGameStateRepository: JpaRepository<CountingGameState, Int>

@Component
class CountingGameStatePersistenceService(val repo: CountingGameStateRepository) {
    @Transactional
    fun upsert(state: CountingGameState): CountingGameState {
        val toSave = repo.findByIdOrNull(state.id)?.apply {
            count = state.count
            lastUser = state.lastUser
        } ?: state
        return repo.save(toSave)
    }

    fun findByIdOrNull(id: Int) = repo.findByIdOrNull(id)
}

interface UserStateRepository: JpaRepository<UserState, String>

@Component
class UserStatePersistenceService(val repo: UserStateRepository) {
    @Transactional
    fun upsert(state: UserState): UserState {
        val toSave = repo.findByIdOrNull(state.userId)?.apply {
            streak = state.streak
            longestStreak = state.longestStreak
            canNotCount = state.canNotCount
        } ?: state
        return repo.save(toSave)
    }

    fun findByIdOrNull(id: String) = repo.findByIdOrNull(id)

    fun findAll(): List<UserState> = repo.findAll()
}
