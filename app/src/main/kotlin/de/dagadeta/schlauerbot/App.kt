package de.dagadeta.schlauerbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableTransactionManagement
class DiscordBotApplication

fun main(args: Array<String>) {
    runApplication<DiscordBotApplication>(*args)
}
