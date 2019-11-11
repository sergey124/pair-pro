package org.vors.pairbot.event.generator

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.vors.pairbot.model.Event
import org.vors.pairbot.model.Team
import org.vors.pairbot.model.UserInfo
import org.vors.pairbot.repository.UserRepository
import org.vors.pairbot.service.TimeService
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import javax.transaction.Transactional


@Component
open class PairGenerator(
        private val timeService: TimeService,
        private val userRepository: UserRepository
) {

    private val LOG = LoggerFactory.getLogger(javaClass)

    @Transactional
    open fun findPair(user: UserInfo, team: Team): Event? {
        val sessionDate = timeService.chooseSessionDate()
        return findPair(user, team, sessionDate)
    }

    @Transactional
    open fun findPair(user: UserInfo, team: Team, sessionDate: Date): Event? {
        val others = findAvailablePeers(user, team, sessionDate)

        if (others.isEmpty()) {
            LOG.debug("Pair not found, no available peers")
            return null
        }
        return pair(user, others, sessionDate)
    }

    private fun findAvailablePeers(user: UserInfo, team: Team, date: Date): List<UserInfo> {
        val dateThreshold = timeService.availableDateTreshold(date)

        return userRepository.findPartnersByNoEventsAfter(dateThreshold, user, team)
    }


    private fun pair(first: UserInfo, others: List<UserInfo>, sessionDate: Date): Event? {
        val random = ThreadLocalRandom.current()

        val othersNotRepeating = others.filter { it.lastPartner != first && first.lastPartner != it }
        if (othersNotRepeating.isEmpty()) {
            LOG.debug("Pair not found, no available peers after filter repeating")
            return null
        }

        val pairIndex = random.nextInt(othersNotRepeating.size)
        val second = othersNotRepeating[pairIndex]
        first.lastPartner = second
        second.lastPartner = first

        return Event(
                first,
                second,
                ThreadLocalRandom.current().nextBoolean(),
                sessionDate
        )
    }

}

