package de.dagadeta.schlauerbot.persistance

import org.springframework.data.jpa.repository.JpaRepository

interface WordChainGameStateRepository: JpaRepository<WordChainGameState, Int>

interface UsedWordRepository: JpaRepository<UsedWord, String>
