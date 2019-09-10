package org.vors.pairbot.telegram.handler.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.vors.pairbot.service.CallbackService;
import org.vors.pairbot.telegram.handler.ChatUpdateHandler;

import static org.vors.pairbot.constant.UpdateHandlerResult.HANDLING_FINISHED_OK;
import static org.vors.pairbot.constant.UpdateHandlerResult.STOP_HANDLING;

@Component
public class CallbackHandler implements ChatUpdateHandler {
    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CallbackService callbackService;

    @Override
    public Object handle(Update update) throws TelegramApiException {
        try {
            callbackService.processKeyboardCallback(update.getCallbackQuery());
        } catch (TelegramApiException e) {
            LOG.error("Cannot process keyboard callback!\n{}", e.toString(), e);
            return STOP_HANDLING;
        }
        return HANDLING_FINISHED_OK;
    }
}
