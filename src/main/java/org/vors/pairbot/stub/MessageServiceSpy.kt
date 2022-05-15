package org.vors.pairbot.stub

import freemarker.template.Configuration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.vors.pairbot.repository.ParticipantRepository
import org.vors.pairbot.repository.UserRepository
import org.vors.pairbot.service.*


@Component
@Primary
@Profile("test_local")
open class MessageServiceSpy(@Autowired
                                   @Qualifier("messageService")
                                   var messageService: MessageService,
                             @Value("\${bot.url}")
                                   botUrl: String, @Lazy
                                   bot: AbsSender,
                             keyboardService: KeyboardService,
                             userService: UserService,
                             freemarkerConfig: Configuration,
                             timeService: TimeService,
                             chatService: ChatService,
                             userRepository: UserRepository,
                             participantRepository: ParticipantRepository,
                             gameService: GameService) :
        MessageService(
                botUrl,
                bot,
                keyboardService,
                userService,
                freemarkerConfig,
                timeService,
                chatService,
                userRepository,
                participantRepository,
                gameService) {

    private var realUserId: Long? = null

    override fun sendMessage(chatId: Long, text: String): Int {

        return messageService.sendMessage(
                getActualRecipient(chatId),
                textWithSentToPrefix(chatId, text))
    }

    override fun sendMessage(sendMessage: SendMessage): Int {
        val initialRecipient = sendMessage.chatId.toLong()
        val forwardedToRecipient = getActualRecipient(initialRecipient)

        sendMessage.chatId = forwardedToRecipient.toString()
        sendMessage.text = textWithSentToPrefix(initialRecipient, sendMessage.text)

        return messageService.sendMessage(sendMessage)
    }

    @Throws(TelegramApiException::class)
    override fun sendMessage(chatId: Long, text: String, keyboard: ReplyKeyboard): Int {
        return messageService.sendMessage(
                getActualRecipient(chatId),
                textWithSentToPrefix(chatId, text),
                keyboard)
    }

    private fun getActualRecipient(chatId: Long) = realUserId?: chatId

    private fun textWithSentToPrefix(chatId: Long, text: String) = "\\[ sent to $chatId ]\n$text"

    fun setRealUserId(realUserId: Long){
        this.realUserId = realUserId
    }
}
