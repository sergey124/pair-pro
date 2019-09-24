package org.vors.pairbot.service

import com.google.common.base.Splitter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CollectionUtils
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.vors.pairbot.constant.Callback
import org.vors.pairbot.model.*
import org.vors.pairbot.repository.EventRepository
import org.vors.pairbot.repository.ParticipantRepository
import org.vors.pairbot.repository.TeamRepository
import org.vors.pairbot.repository.UserRepository

import java.util.Date
import java.util.UUID

import org.vors.pairbot.constant.BotConstants.CALLBACK_DATA_SEPARATOR
import org.vors.pairbot.model.EventStatus.*

@Transactional
@Component
class CallbackService(
        @Autowired
        @Lazy
        var bot: AbsSender,
        @Autowired
        var userService: UserService,
        @Autowired
        var eventRepository: EventRepository,
        @Autowired
        var teamRepository: TeamRepository,
        @Autowired
        var messageService: MessageService,
        @Autowired
        var userRepository: UserRepository,
        @Autowired
        var keyboardService: KeyboardService,
        @Autowired
        var participantRepository: ParticipantRepository
) {
    val LOG: Logger = LoggerFactory.getLogger(CommandService::class.java)

    @Throws(TelegramApiException::class)
    fun processKeyboardCallback(callbackquery: CallbackQuery) {
        val callbackParts = extractCallbackParts(callbackquery)
        if (CollectionUtils.isEmpty(callbackParts)) {
            return
        }

        val userId = callbackquery.from.id

        val userOpt = userService.findByUserId(userId)

        if (!userOpt.isPresent) {
            return
        }

        var answerText: String? = null
        val user = userOpt.get()
        val chatId = callbackquery.message.chatId

        when (Callback.valueOf(callbackParts[0])) {
            Callback.NEW_TEAM -> {
                val team = newTeam(user)
                sendJoinLink(chatId, team)
            }
            Callback.ADD_TO_TEAM -> answerText = "ask your peers for a link"
            Callback.ACCEPT_DECLINE -> {
                val eventPk = java.lang.Long.valueOf(callbackParts[1])

                val participant = participantRepository.getOne(ParticipantId(user.pk, eventPk))

                val accepted = java.lang.Boolean.valueOf(callbackParts[2])

                participant.accepted = if (accepted) ACCEPTED else DECLINED

                participantRepository.save(participant)

                if (!accepted) {
                    user.lastDeclineDate = Date()
                    userRepository.save(user)
                }

                val event = participant.event
                updateEvent(event, participant.accepted)
                updateInvite(event)

                answerText = "ok"
            }
            Callback.VOID -> TODO()
        }
        sendAnswerCallbackQuery(answerText, false, callbackquery)
    }

    private fun updateEvent(event: Event, participantAccept: EventStatus) {
        val responses = event.participants.map { it.accepted }

        if (participantAccept == DECLINED) {
            event.accepted = DECLINED
        } else if (responses.all { it == ACCEPTED }) {
            event.accepted = ACCEPTED
        }

        if (event.accepted != NO_RESPONSE) {
            eventRepository.save(event)
        }
    }

    private fun updateInvite(event: Event) {
        messageService.updateToAll(
                event,
                messageService::pairDescriptionText,
                keyboardService::acceptedInviteKeyboard)
    }

    private fun newTeam(creator: UserInfo): Team {
        val team = Team(
                UUID.randomUUID(),
                creator
        )
        team.addMember(creator)
//        team.addMember(newDummyUser())

        teamRepository.save(team)
        return team
    }

    /**
     * For testing when there's no second account
     */
    private fun newDummyUser(): UserInfo {
        val user = UserInfo(0, "Partner")
        userRepository.save(user)
        return user
    }


    @Throws(TelegramApiException::class)
    private fun sendAnswerCallbackQuery(text: String?, alert: Boolean, callbackquery: CallbackQuery) {
        val answerCallbackQuery = AnswerCallbackQuery()
        answerCallbackQuery.callbackQueryId = callbackquery.id
        answerCallbackQuery.showAlert = alert
        answerCallbackQuery.text = text
        bot.execute(answerCallbackQuery)
    }

    private fun extractCallbackParts(callbackquery: CallbackQuery): List<String> {
        val callbackData = callbackquery.data

        return Splitter.on(CALLBACK_DATA_SEPARATOR).splitToList(callbackData)
    }

    private fun sendJoinLink(chatId: Long, team: Team) {
        try {
            messageService.sendMessage(chatId, messageService.getJoinTeamText(team))
        } catch (e: TelegramApiException) {
            LOG.error("Sending join link failed", e)
        }

    }

}
