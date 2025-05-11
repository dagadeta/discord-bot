package de.dagadeta.schlauerbot.persistance

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity(name = "game_state")
data class WordChainGameState(@Id val id: Int, var started: Boolean, var lastUser: String)

@Entity(name = "used_words")
data class UsedWord(@Id val word: String)