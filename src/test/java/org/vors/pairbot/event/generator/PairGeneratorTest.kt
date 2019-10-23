package org.vors.pairbot.event.generator

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import org.vors.pairbot.PairbotApplication
import org.vors.pairbot.model.Event
import org.vors.pairbot.model.Team
import org.vors.pairbot.model.UserInfo
import org.vors.pairbot.repository.EventRepository
import org.vors.pairbot.repository.TeamRepository
import org.vors.pairbot.repository.UserRepository
import org.vors.pairbot.service.TimeService
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ThreadLocalRandom

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [PairbotApplication::class])
@Transactional
class PairGeneratorTest {
    
    private val format = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    private lateinit var sessionDate: Date
    private lateinit var team: Team
    private lateinit var user: UserInfo
    private lateinit var user_noRecentEvent: UserInfo
    private lateinit var user_recentEvent: UserInfo

    @Autowired
    lateinit var systemUnderTest: PairGenerator
    @Autowired
    lateinit var teamRepository: TeamRepository
    @Autowired
    lateinit var eventRepository: EventRepository
    @Autowired
    lateinit var timeService: TimeService
    @Autowired
    lateinit var userRepository: UserRepository
    @Value("\${event.interval.min.seconds}")
    var minSecondsBetweenSessions: Int = -1
    
    @Before
    @Throws(Exception::class)
    fun setUp() {
        sessionDate = format.parse("01-01-2000")
        val oldSessionDate = datePlusSeconds(sessionDate, -minSecondsBetweenSessions - 1)
        val recentSessionDate = datePlusSeconds(sessionDate, -minSecondsBetweenSessions + 1)

        user = UserInfo(0, "Vasya")
        user_noRecentEvent = newDummyUser()
        user_recentEvent = newDummyUser()
        userRepository.saveAll(listOf(user, user_noRecentEvent, user_recentEvent))

        var event = Event(user, user_noRecentEvent, true, oldSessionDate)
        event.date = oldSessionDate
        eventRepository.save(event)

        event = Event(user, user_recentEvent, true, recentSessionDate)
        eventRepository.save(event)

        team = Team(UUID.randomUUID(), user)
        team.addMember(user)

    }

    @Test
    fun givenPartnerHasNoRecentEvents_whenFindPair_thenFound() {
        //given
        team.addMember(user_noRecentEvent)
        teamRepository.save(team)

        //when
        val pair = systemUnderTest.findPair(user, team, sessionDate)

        //then
        val actual = pair?.partner
        Assert.assertEquals(user_noRecentEvent, actual)
    }

    @Test
    fun givenOneOfPartnersHasNoRecentEvents_whenFindPair_thenFindThisMember() {
        //given
        team.addMember(user_recentEvent)
        team.addMember(user_noRecentEvent)
        teamRepository.save(team)

        //when
        val pair = systemUnderTest.findPair(user, team, sessionDate)

        //then
        val actual = pair?.partner
        Assert.assertEquals(user_noRecentEvent, actual)
    }

    @Test
    fun givenAllPartnersHaveRecentEvent_whenFindPair_thenNotFound() {
        //given
        team.addMember(user_recentEvent)
        teamRepository.save(team)

        //when
        val pair = systemUnderTest.findPair(user, team, sessionDate)

        //then
        Assert.assertTrue(pair == null)
    }


    private fun newDummyUser(): UserInfo {
        val randomInt = ThreadLocalRandom.current().nextInt()

        return UserInfo(randomInt, "" + randomInt)
    }

    private fun datePlusSeconds(date: Date, amount: Int): Date {
        return Date.from(date.toInstant().plus(amount.toLong(), ChronoUnit.SECONDS))
    }

}