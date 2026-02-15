package de.dagadeta.schlauerbot.externalservices

import de.dagadeta.schlauerbot.discord.Logging
import org.springframework.stereotype.Service

@Service
class CouldYouGetter(logger: Logging) : HttpGetter(
    logger = logger,
    url = "https://naas.isalman.dev/no",
    propertyName = "reason",
    serviceName = "No-as-a-Service",
)

@Service
class RandomUselessFactGetter(logger: Logging) : HttpGetter(
    logger = logger,
    url = "https://uselessfacts.jsph.pl/random.json",
    propertyName = "text",
    serviceName = "Random-Useless-Facts",
)

@Service
class RandomCatFactGetter(logger: Logging) : HttpGetter(
    logger = logger,
    url = "https://meowfacts.herokuapp.com/",
    propertyName = "data[0]",
    serviceName = "Random-Cat-Facts",
)
