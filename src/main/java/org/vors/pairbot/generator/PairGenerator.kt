package org.vors.pairbot.generator

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.vors.pairbot.model.Event
import org.vors.pairbot.model.Participant
import org.vors.pairbot.model.UserInfo
import org.vors.pairbot.repository.EventRepository
import org.vors.pairbot.repository.ParticipantRepository
import org.vors.pairbot.repository.UserRepository
import org.vors.pairbot.service.TimeService

import java.util.Date
import java.util.Optional
import java.util.concurrent.ThreadLocalRandom

@Component
class PairGenerator {
    private val LOG = LoggerFactory.getLogger(javaClass)

    private val timeService: TimeService
    private val eventRepository: EventRepository
    private val participantRepository: ParticipantRepository
    private val userRepository: UserRepository

    constructor(
            timeService: TimeService,
            eventRepository: EventRepository,
            participantRepository: ParticipantRepository,
            userRepository: UserRepository) {
        this.timeService = timeService
        this.eventRepository = eventRepository
        this.participantRepository = participantRepository
        this.userRepository = userRepository
    }

    fun findPair(user: UserInfo): Optional<Event> {
        val sessionDate = timeService.chooseSessionDate()
        return findPair(user, sessionDate)
    }

    fun findPair(user: UserInfo, sessionDate: Date): Optional<Event> {
        val others = findAvailablePeers(user, sessionDate)

        if (others.isEmpty()) {
            LOG.debug("Pair not found, no available peers")
            return Optional.empty()
        }
        val event = pair(user, others, sessionDate)

        return Optional.of(event)
    }

    private fun findAvailablePeers(user: UserInfo, date: Date): List<UserInfo> {
        val dateThreshold = timeService.beginningOfDateMinusDaysFrom(date, MIN_DAYS_BETWEEN_SESSIONS)

        return userRepository.findByNoEventsAfter(dateThreshold, user, user.team)
    }

    private fun pair(first: UserInfo, others: List<UserInfo>, sessionDate: Date): Event {

        val random = ThreadLocalRandom.current()
        val pairIndex = random.nextInt(others.size)
        val second = others[pairIndex]

        val event = Event(
                first,
                second,
                ThreadLocalRandom.current().nextBoolean(),
                sessionDate
        )

        event.addParticipant(first)
        event.addParticipant(second)

        return event
    }

    fun canCreateEvent(user: UserInfo): Boolean {
        return !hasDeclinedRecently(user) && !hasUpcomingEvents(user)
    }

    private fun hasUpcomingEvents(user: UserInfo): Boolean {
        return eventRepository.existsByDateAfterAndParticipants_User(Date(), user)
    }

    fun hasDeclinedRecently(user: UserInfo): Boolean {
        val lastDecline = user.lastDeclineDate
        return lastDecline != null && lastDecline.after(timeService.lastDeclineThreshold())
    }

    companion object {
        internal val MIN_DAYS_BETWEEN_SESSIONS = 4
    }

}

