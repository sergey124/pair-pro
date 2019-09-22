package org.vors.pairbot.telegram.handler.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.vors.pairbot.service.CommandService
import org.vors.pairbot.service.MessageService
import org.vors.pairbot.telegram.handler.ChatUpdateHandler

import org.vors.pairbot.constant.UpdateHandlerResult.HANDLING_FINISHED_OK

@Component
class CommandHandler : ChatUpdateHandler {

    @Autowired
    private val commandService: CommandService? = null
    @Autowired
    private val messageService: MessageService? = null

    @Throws(TelegramApiException::class)
    override fun handle(update: Update): Any {

        val message = update.message

        commandService!!.processCommand(message)

        return HANDLING_FINISHED_OK
    }
}
