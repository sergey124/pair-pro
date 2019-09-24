package org.vors.pairbot.event.generator

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import org.vors.pairbot.PairbotApplication
import org.vors.pairbot.constant.BotConstants.MIN_DAYS_BETWEEN_SESSIONS
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

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [PairbotApplication::class])
@Transactional
class PairGeneratorTest {

    private val format = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    private var sessionDate: Date? = null
    private var team: Team? = null
    private var user: UserInfo? = null
    private var member_noRecentEvent: UserInfo? = null
    private var member_recentEvent: UserInfo? = null

    @Autowired
    private val systemUnderTest: PairGenerator? = null
    @Autowired
    private val teamRepository: TeamRepository? = null
    @Autowired
    private val eventRepository: EventRepository? = null
    @Autowired
    private val userRepository: UserRepository? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        sessionDate = format.parse("01-01-2000")
        val oldSessionDate = datePlusDays(sessionDate!!, -MIN_DAYS_BETWEEN_SESSIONS - 1)
        val recentSessionDate = datePlusDays(sessionDate!!, -MIN_DAYS_BETWEEN_SESSIONS + 1)

        user = UserInfo(0, "Vasya")
        member_noRecentEvent = newDummyUser(oldSessionDate)
        member_recentEvent = newDummyUser(recentSessionDate)

        var event = Event(user!!, member_noRecentEvent!!, true, oldSessionDate)
        event.addParticipant(member_noRecentEvent!!)
        event.date = oldSessionDate
        eventRepository!!.save(event)

        event = Event(user!!, member_recentEvent!!, true, recentSessionDate)
        eventRepository.save(event)

        team = Team(UUID.randomUUID(), user!!)
        team!!.addMember(user!!)

    }

    @Test
    fun givenPartnerHasNoRecentEvents_whenFindPair_thenFound() {
        //given
        team!!.addMember(member_noRecentEvent!!)
        teamRepository!!.save(team!!)

        //when
        val pair = systemUnderTest!!.findPair(user!!, team!!, sessionDate!!)

        //then
        val actual = pair?.partner
        Assert.assertEquals(member_noRecentEvent, actual)
    }

    @Test
    fun givenOneOfPartnersHasNoRecentEvents_whenFindPair_thenFindThisMember() {
        //given
        team!!.addMember(member_recentEvent!!)
        team!!.addMember(member_noRecentEvent!!)
        teamRepository!!.save(team!!)

        //when
        val pair = systemUnderTest!!.findPair(user!!, team!!, sessionDate!!)

        //then
        val actual = pair?.partner
        Assert.assertEquals(member_noRecentEvent, actual)
    }

    @Test
    fun givenAllPartnersHaveRecentEvent_whenFindPair_thenNotFound() {
        //given
        team!!.addMember(member_recentEvent!!)
        teamRepository!!.save(team!!)

        //when
        val pair = systemUnderTest!!.findPair(user!!, team!!, sessionDate!!)

        //then
        Assert.assertTrue(pair == null)
    }


    private fun newDummyUser(lastSessionDate: Date): UserInfo {
        val randomInt = ThreadLocalRandom.current().nextInt()

        return UserInfo(randomInt, "" + randomInt)
    }

    private fun datePlusDays(date: Date, days: Int): Date {
        return Date.from(date.toInstant().plus(days.toLong(), ChronoUnit.DAYS))
    }

}