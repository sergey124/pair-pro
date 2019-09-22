package org.vors.pairbot.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.objects.User
import org.vors.pairbot.model.Event
import org.vors.pairbot.model.UserInfo
import org.vors.pairbot.repository.EventRepository
import org.vors.pairbot.repository.UserRepository

import java.time.Duration
import java.util.Date
import java.util.Optional

@Transactional
@Component
class UserService {
    private val LOG = LoggerFactory.getLogger(javaClass)

    @Autowired
    private val userRepository: UserRepository? = null
    @Autowired
    private val eventRepository: EventRepository? = null
    @Autowired
    private val timeService: TimeService? = null

    fun createAndSaveUser(user: User): UserInfo {
        return userRepository!!.save(createUserInfo(user))
    }

    private fun createUserInfo(user: User): UserInfo {
        val userId = user.id
        val firstName = user.firstName
        val lastName = user.lastName

        return createUserInfo(userId, firstName, lastName)
    }

    private fun createUserInfo(userId: Int?, firstName: String, lastName: String): UserInfo {
        return newUserInfo(userId, firstName, lastName)
    }

    private fun newUserInfo(id: Int?, firstName: String, lastName: String): UserInfo {
        val newUser = UserInfo()
        newUser.userId = id
        newUser.firstName = firstName
        newUser.lastName = lastName
        newUser.createdDate = Date()
        return newUser
    }

    fun getExistingUser(userId: Int?): UserInfo {
        return findByUserId(userId).orElseThrow { IllegalStateException("Existing user not found for ID: " + userId!!) }
    }

    fun findByUserId(userId: Int?): Optional<UserInfo> {
        return userRepository!!.findByUserId(userId)
    }

    fun findUpcomingEvents(upcomingIn: Duration, scanPeriod: Duration): List<Event> {
        return eventRepository!!.findByDateBetweenAndAcceptedTrue(
                timeService!!.nowPlusDuration(upcomingIn.minus(scanPeriod)),
                timeService.nowPlusDuration(upcomingIn))
    }

}
