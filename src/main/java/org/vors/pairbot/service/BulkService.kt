package org.vors.pairbot.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.vors.pairbot.event.EventOrganizer
import java.util.*

@Component
class BulkService(val userService: UserService,
                  val timeService: TimeService,
                  val eventOrganizer: EventOrganizer){

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    fun scheduleSessionForAll(){
        val users = userService.findByNoEventsAfter(timeService.availableDateTreshold(Date()))

        logger.info("Found ${users.size} users to schedule sessions")
        for (user in users) {
            eventOrganizer.tryOrganizeEvent(user)
        }
    }
}