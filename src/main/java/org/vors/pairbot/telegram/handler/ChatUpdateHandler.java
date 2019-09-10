package org.vors.pairbot.telegram.handler;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Chain of handlers is to be executed when bot gets telegram chat update.
 */
public interface ChatUpdateHandler {
    Object handle(Update update) throws TelegramApiException;
}
