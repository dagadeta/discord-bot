package de.dagadeta.schlauerbot.externalservices

import de.dagadeta.schlauerbot.botconfig.Bottalking
import de.dagadeta.schlauerbot.discord.Logging
import de.dagadeta.schlauerbot.discord.PermissionValidator
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
    answerProvider = couldYouGetter,
    commandName = "could-you",
    serviceName = CouldYou::class.simpleName,
    serviceDescription = "No, but creative",
    permissionValidator = PermissionValidator(),
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
    answerProvider = randomUselessFactGetter,
    commandName = "random-useless-fact",
    serviceName = RandomUselessFact::class.simpleName,
    serviceDescription = "Tells you a random useless fact",
    permissionValidator = PermissionValidator(),
)

@Service
class RandomCatFact(
    bottalking: Bottalking,
    logging: Logging,
    api: JDA,
    randomCatFactGetter: RandomCatFactGetter,
) : ExternalServiceCaller(
    bottalking,
    logging,
    api,
    answerProvider = randomCatFactGetter,
    commandName = "random-cat-fact",
    serviceName = RandomCatFact::class.simpleName,
    serviceDescription = "Tells you a random cat fact",
    permissionValidator = PermissionValidator(),
)

@Service
class DingDong(
    bottalking: Bottalking,
    logging: Logging,
    api: JDA,
) : ExternalServiceCaller(
    bottalking,
    logging,
    api,
    answerProvider = { "Dong!" },
    commandName = "ding",
    serviceName = DingDong::class.simpleName,
    serviceDescription = "Answers Dong",
    permissionValidator = PermissionValidator(),
)
