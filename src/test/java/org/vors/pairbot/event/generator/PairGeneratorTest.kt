package org.vors.pairbot.event.generator

import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.vors.pairbot.PairbotApplication
import org.vors.pairbot.model.Event
import org.vors.pairbot.model.Team
import org.vors.pairbot.model.UserInfo
import org.vors.pairbot.repository.EventRepository
import org.vors.pairbot.repository.TeamRepository
import org.vors.pairbot.repository.UserRepository
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import javax.persistence.EntityManager
import javax.transaction.Transactional

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = [PairbotApplication::class])
class PairGeneratorTest {

    private val format = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    private lateinit var sessionDate: Date
    private lateinit var team: Team
    private lateinit var user: UserInfo
    private lateinit var user_noRecentEvent: UserInfo
    private lateinit var user2_noRecentEvent: UserInfo
    private lateinit var user_recentEvent: UserInfo

    @Autowired
    lateinit var systemUnderTest: PairGenerator

    @Autowired
    lateinit var teamRepository: TeamRepository

    @Autowired
    lateinit var eventRepository: EventRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var entityManager: EntityManager

    @Value("\${event.interval.min.seconds}")
    var minSecondsBetweenSessions: Int = -1

    @BeforeEach
    fun setUp() {
        sessionDate = format.parse("01-01-2000")
        val oldSessionDate = datePlusSeconds(sessionDate, -minSecondsBetweenSessions - 1)
        val recentSessionDate = datePlusSeconds(sessionDate, -minSecondsBetweenSessions + 1)

        user = UserInfo(0, "Vasya")
        user_noRecentEvent = newDummyUser()
        user2_noRecentEvent = newDummyUser()
        user_recentEvent = newDummyUser()
        userRepository.saveAll(listOf(user, user_noRecentEvent, user2_noRecentEvent, user_recentEvent))

        var event = Event(user, user_noRecentEvent, true, oldSessionDate)
        event.date = oldSessionDate

        entityManager.merge(event.creator)
        entityManager.merge(event.partner)
        eventRepository.save(event)

        event = Event(user, user_recentEvent, true, recentSessionDate)
        eventRepository.save(event)

        team = Team(UUID.randomUUID(), user)
        team.addMember(user)
    }

    @Test
    @Transactional
    fun givenPartnerHasNoRecentEvents_whenFindPair_thenFound() {
        //given
        team.addMember(user_noRecentEvent)
        teamRepository.save(team)

        //when
        val pair = systemUnderTest.findPair(user, team, sessionDate)

        //then
        val actual = pair?.partner
        Assertions.assertEquals(user_noRecentEvent, actual)
    }

    @Test
    @Transactional
    fun givenOneOfPartnersHasNoRecentEvents_whenFindPair_thenFindThisMember() {
        //given
        team.addMember(user_recentEvent)
        team.addMember(user_noRecentEvent)
        teamRepository.save(team)

        //when
        val pair = systemUnderTest.findPair(user, team, sessionDate)

        //then
        val actual = pair?.partner
        Assertions.assertEquals(user_noRecentEvent, actual)
    }

    @Test
    @Transactional
    fun givenAllPartnersHaveRecentEvent_whenFindPair_thenNotFound() {
        //given
        team.addMember(user_recentEvent)
        teamRepository.save(team)

        //when
        val pair = systemUnderTest.findPair(user, team, sessionDate)

        //then
        Assertions.assertTrue(pair == null)
    }

    @Test
    @Transactional
    fun `given last session with the only member, when findPair, then no pairs found`() {
        //given
        user.lastPartner = user_noRecentEvent
        userRepository.save(user)
        team.addMember(user_noRecentEvent)
        teamRepository.save(team)

        //when
        val pair = systemUnderTest.findPair(user, team)

        //then
        Assertions.assertTrue(pair == null)
    }

    @Test
    @Transactional
    fun `given last session of the only member was with user, when findPair, then no pairs found`() {
        //given
        user_noRecentEvent.lastPartner = user
        userRepository.save(user_noRecentEvent)
        team.addMember(user_noRecentEvent)
        teamRepository.save(team)

        //when
        val pair = systemUnderTest.findPair(user, team)

        //then
        Assertions.assertTrue(pair == null)
    }

    @Test
    @Transactional
    fun `given only 1 partner available, when findPair twice after interval, then no pairs found for second attempt`() {
        //given
        team.addMember(user_noRecentEvent)
        teamRepository.save(team)

        //when
        val firstAttemptPair = systemUnderTest.findPair(user, team, Date())
        val secondAttemptPair = systemUnderTest.findPair(user, team)

        //then
        Assertions.assertNotNull(firstAttemptPair, "first time pair should be created")
        Assertions.assertNull(secondAttemptPair, "second time pair should not be created, as only the same partner available")
    }

    @Test
    @Transactional
    fun `given 2 partners available, when findPair twice after interval, then second pair different partner`() {
        //given
        team.addMember(user_noRecentEvent)
        team.addMember(user2_noRecentEvent)
        teamRepository.save(team)

        //when
        val firstAttemptPair = systemUnderTest.findPair(user, team, Date())
        val secondAttemptPair = systemUnderTest.findPair(user, team)

        //then
        Assertions.assertNotEquals(firstAttemptPair!!.partner, secondAttemptPair!!.partner, "second pair with different partner")
    }

    private fun newDummyUser(): UserInfo {
        val randomInt = ThreadLocalRandom.current().nextInt()

        return UserInfo(randomInt, "" + randomInt)
    }

    private fun datePlusSeconds(date: Date, amount: Int): Date {
        return Date.from(date.toInstant().plus(amount.toLong(), ChronoUnit.SECONDS))
    }

}