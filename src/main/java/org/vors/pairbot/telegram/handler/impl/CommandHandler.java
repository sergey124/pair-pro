package org.vors.pairbot.telegram.handler.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.vors.pairbot.service.CommandService;
import org.vors.pairbot.service.MessageService;
import org.vors.pairbot.telegram.handler.ChatUpdateHandler;

import static org.vors.pairbot.constant.UpdateHandlerResult.HANDLING_FINISHED_OK;

@Component
public class CommandHandler implements ChatUpdateHandler {

    @Autowired
    private CommandService commandService;
    @Autowired
    private MessageService messageService;

    @Override
    public Object handle(Update update) throws TelegramApiException {

        Message message = update.getMessage();

        commandService.processCommand(message);

        return HANDLING_FINISHED_OK;
    }
}
