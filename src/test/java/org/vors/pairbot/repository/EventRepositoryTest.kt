package org.vors.pairbot.repository

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.vors.pairbot.PairbotApplication
import org.vors.pairbot.model.Event
import org.vors.pairbot.model.Participant
import org.vors.pairbot.model.UserInfo
import org.vors.pairbot.service.TimeService

import java.util.Date

import org.junit.Assert.*

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [PairbotApplication::class])
class EventRepositoryTest {

    @Autowired
    private val systemUnderTest: EventRepository? = null

    private val timeService = TimeService()
    private val date = Date()
    private var event: Event? = null
    private var user: UserInfo? = null

    @Before
    fun setup() {
        user = UserInfo(0, "Vasya")
        val partner = UserInfo(0, "Petya")

        event = Event(user!!, partner, true, Date())
        event!!.addParticipant(user!!)
    }

    @Test
    fun givenEventBeforeGivenDate_whenExistsByDate_thenFalse() {
        //given
        saveEventWithDateShift(-1)

        //then
        assertFalse(systemUnderTest!!.existsByDateAfterAndParticipants_User(date, user!!))
    }

    @Test
    fun givenEventAfterGivenDate_whenExistsByDate_thenTrue() {
        //given
        saveEventWithDateShift(1)

        //then
        assertTrue(systemUnderTest!!.existsByDateAfterAndParticipants_User(date, user!!))
    }

    private fun saveEventWithDateShift(hours: Int) {
        event!!.date = timeService.datePlusHours(date, hours)
        systemUnderTest!!.save(event!!)
    }

}