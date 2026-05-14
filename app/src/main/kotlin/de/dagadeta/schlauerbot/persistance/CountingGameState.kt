package de.dagadeta.schlauerbot.persistance

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(schema = "countinggame", name = "game_state")
data class CountingGameState(@Id val id: Int, var count: Int, var lastUser: String)
