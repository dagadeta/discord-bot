package de.dagadeta.schlauerbot.persistance

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(schema = "wordchaingame", name = "game_state")
data class WordChainGameState(@Id val id: Int, var started: Boolean, var lastUser: String)

@Entity
@Table(schema = "wordchaingame", name = "used_words")
data class UsedWord(@Id val word: String)
