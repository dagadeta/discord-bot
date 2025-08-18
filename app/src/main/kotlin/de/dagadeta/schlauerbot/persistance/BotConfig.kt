package de.dagadeta.schlauerbot.persistance

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(schema = "discordbot", name = "bot_config")
@IdClass(ConfigId::class)
data class BotConfig(@Id val commandGroup: String, @Id val name: String, var value: String, var timestamp: LocalDateTime = LocalDateTime.now())

data class ConfigId(val commandGroup: String, val name: String) {
    constructor() : this("", "")
}
