package org.vors.pairbot.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.vors.pairbot.model.Event
import org.vors.pairbot.model.Participant
import org.vors.pairbot.repository.UserRepository
import java.time.Duration
import javax.transaction.Transactional

@Transactional
@Component
open class NotificationService(
        open var userService: UserService,
        open var chatService: ChatService,
        open var messageService: MessageService,
        open var userRepository: UserRepository
) {
    open var log = LoggerFactory.getLogger(javaClass)

    fun notifyAllUpcomingIn(expiringIn: Duration, scanPeriod: Duration) {
        log.info("Start notify about upcoming events")
        val events = userService.findUpcomingEvents(expiringIn, scanPeriod)
        log.info("Found {} people to notify", events.size)

        events.forEach { this.notifyUpcoming(it) }

        log.info("End notify about upcoming events")
    }

    private fun notifyUpcoming(event: Event) {
        try {
            event.participants.forEach { this.notifyUpcoming(it) }
        } catch (e: Exception) {
            log.error("Can't notify about upcoming event pk = {}\n{}", event.pk, e.toString(), e)
        }

    }

    private fun notifyUpcoming(participant: Participant) {
        val user = participant.user
        val userId = user.userId
        log.info("Notifying upcoming user {}", userId)
        try {
            val sentMessage = messageService.sendMessage(
                    messageService.getUpcomingNotificationMessage(participant))

            log.info("Notified upcoming user {}, id {}", user.firstName, userId)

            user.lastMessageId = sentMessage

            userRepository.save(user)
        } catch (e: TelegramApiException) {
            log.error("Can't notify expiring user {}\n{}", user.pk, e.toString(), e)
        }

    }

}
