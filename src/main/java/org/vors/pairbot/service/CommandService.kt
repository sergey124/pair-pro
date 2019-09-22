package org.vors.pairbot.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.objects.EntityType
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.MessageEntity
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.vors.pairbot.constant.BotCommand
import org.vors.pairbot.generator.PairGenerator
import org.vors.pairbot.model.UserInfo
import org.vors.pairbot.repository.EventRepository
import org.vors.pairbot.repository.TeamRepository
import java.util.*


@Transactional
@Component
class CommandService(
        private val userService: UserService,
        private val messageService: MessageService,
        private val keyboardService: KeyboardService,
        private val pairGenerator: PairGenerator,
        private val chatService: ChatService,
        private val eventRepository: EventRepository,
        private val teamRepository: TeamRepository
) {
    private val LOG = LoggerFactory.getLogger(CommandService::class.java)


    @Throws(TelegramApiException::class)
    fun processCommand(message: Message) {
        val commandTextOpt = extractCommandText(message)

        if (commandTextOpt.isPresent) {
            val commandText = commandTextOpt.get()

            val chatId = message.chatId
            val user = userService.findByUserId(message.from.id).get()
            when (BotCommand.valueOf(commandText.toUpperCase())) {
                BotCommand.START -> {
                    val messageText = message.text
                    if (commandText.length < messageText.length) {
                        val teamToken = messageText.substring(commandText.length + 1)
                        joinTeamByToken(teamToken, user)

                        messageService.sendMessage(chatId, messageService.teamInfo(user))

                    } else {
                        val sendMessage = messageService.getMessageWithKeyboard(
                                chatId,
                                "Hi! Let's make a team. \nOr ask your peers for a link to join",
                                keyboardService.startKeyboard)
                        messageService.sendMessage(sendMessage)
                    }
                }
                BotCommand.PAIR -> if (pairGenerator.canCreateEvent(user)) {
                    createEventAndInvite(user)
                } else {
                    messageService.sendMessage(chatId, messageService.tryLaterText(user))
                }
                BotCommand.MYTEAM -> messageService.sendMessage(chatId, messageService.teamInfo(user))
                BotCommand.SET_LOCATION -> messageService.requestLocation(chatId)
                BotCommand.VOID -> TODO()
            }
        }
    }

    private fun joinTeamByToken(token: String, user: UserInfo) {
        val teamOpt = teamRepository.findByToken(UUID.fromString(token))

        teamOpt.ifPresent { team ->
            team.addMember(user)
            teamRepository.save(team)
        }
    }

    @Throws(TelegramApiException::class)
    private fun createEventAndInvite(user: UserInfo) {
        val eventOpt = pairGenerator.findPair(user)

        if (eventOpt.isPresent) {
            val event = eventOpt.get()
            eventRepository.save(event)

            messageService.sendToAll(event, messageService::inviteText, keyboardService::getInviteKeyboard)

        } else {
            sendNoPairsAvailable(user)
        }
    }


    @Throws(TelegramApiException::class)
    private fun sendNoPairsAvailable(user: UserInfo) {
        val message = messageService.getMessage(
                chatService.getPrivateChatId(user),
                "No pairs available.\nEveryone is busy or no one joined yet \uD83D\uDE1E"
        )
        messageService.sendMessage(message)
    }

    private fun extractCommandText(message: Message): Optional<String> {
        val entitiesOpt = Optional.ofNullable(message.entities)

        return entitiesOpt.flatMap(this::extractCommandText)
    }

    private fun extractCommandText(entities: List<MessageEntity>): Optional<String> {
        return entities.stream()
                .filter { e -> e != null && e.offset == 0 && EntityType.BOTCOMMAND == e.type }
                .findFirst()
                .map { this.removeMention(it.text) }
    }

    private fun removeMention(commandText: String): String {
        val indexOfMention = commandText.indexOf("@")
        if (indexOfMention != -1) {
            return commandText.substring(0, indexOfMention)
        }
        return commandText
    }

}
