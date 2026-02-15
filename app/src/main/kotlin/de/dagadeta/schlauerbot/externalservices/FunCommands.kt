package de.dagadeta.schlauerbot.externalservices

import de.dagadeta.schlauerbot.botconfig.Bottalking
import de.dagadeta.schlauerbot.discord.Logging
import net.dv8tion.jda.api.JDA
import org.springframework.stereotype.Service

@Service
class CouldYou(
    bottalking: Bottalking,
    logging: Logging,
    api: JDA,
    couldYouGetter: CouldYouGetter,
) : ExternalServiceCaller(
    bottalking,
    logging,
    api,
    externalService = couldYouGetter,
    commandName = "could-you",
    serviceName = CouldYou::class.simpleName,
    serviceDescription = "No, but creative",
)

@Service
class RandomUselessFact(
    bottalking: Bottalking,
    logging: Logging,
    api: JDA,
    randomUselessFactGetter: RandomUselessFactGetter,
) : ExternalServiceCaller(
    bottalking,
    logging,
    api,
    externalService = randomUselessFactGetter,
    commandName = "random-useless-fact",
    serviceName = RandomUselessFact::class.simpleName,
    serviceDescription = "Tells you a random useless fact",
)
