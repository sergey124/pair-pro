package org.vors.pairbot.telegram.handler.impl


import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.vors.pairbot.constant.UpdateHandlerResult.HANDLING_FINISHED_OK
import org.vors.pairbot.constant.UpdateHandlerResult.STOP_HANDLING
import org.vors.pairbot.service.CallbackService
import org.vors.pairbot.telegram.handler.ChatUpdateHandler

@Component
class CallbackHandler(
      var callbackService: CallbackService) : ChatUpdateHandler {
    private val LOG = LoggerFactory.getLogger(this.javaClass)

    @Throws(TelegramApiException::class)
    override fun handle(update: Update): Any {
        try {
            callbackService.processKeyboardCallback(update.callbackQuery)
        } catch (e: TelegramApiException) {
            LOG.error("Cannot process keyboard callback!\n{}", e.toString(), e)
            return STOP_HANDLING
        }

        return HANDLING_FINISHED_OK
    }
}
