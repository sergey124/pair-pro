package org.vors.pairbot.telegram.handler.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.vors.pairbot.constant.UpdateHandlerResult.HANDLING_FINISHED_OK
import org.vors.pairbot.service.KeyboardService
import org.vors.pairbot.service.MessageService
import org.vors.pairbot.service.TimeZoneService
import org.vors.pairbot.service.UserService
import org.vors.pairbot.telegram.handler.ChatUpdateHandler
import java.time.ZoneId

@Component
class SetLocationHandler : ChatUpdateHandler {

    @Autowired
    private val messageService: MessageService? = null
    @Autowired
    private val userService: UserService? = null
    @Autowired
    private val timeZoneService: TimeZoneService? = null
    @Autowired
    private val keyboardService: KeyboardService? = null

    @Throws(TelegramApiException::class)
    override fun handle(update: Update): Any {
        val message = update.message
        val user = userService!!.getExistingUser(message.from.id)

        val location = message.location

        val maybeZone = timeZoneService!!.setTimeZone(location.latitude!!, location.longitude!!, user)

        val text = "Time zone set as: " + maybeZone.orElse(ZoneId.systemDefault())
        messageService!!.sendMessage(message.chatId, text, keyboardService!!.removeCustomKeyboard())

        return HANDLING_FINISHED_OK
    }
}
