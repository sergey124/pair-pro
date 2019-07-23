package org.vors.pairbot.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;


@Component
public class PairBot extends TelegramLongPollingBot {
    private Logger LOG = LoggerFactory.getLogger(PairBot.class);
    @Value("${bot.token}")
    private String botToken;
    @Value("${bot.username}")
    private String botUsername;
    private Integer botUserId = null;

    @Override
    public void onUpdateReceived(Update update) {
        try {
            callHandlers(update);

        } catch (RuntimeException e) {
            LOG.error("Update handling failed!", e);
        }
    }

    private void callHandlers(Update update) {
        if (update.getMessage() != null) {
            LOG.info("Handling update for chat {}", update.getMessage().getChatId());
        }

    }

    public Integer getBotUserId() {
        return botUserId;
    }

    public void setBotUserId(Integer botUserId) {
        this.botUserId = botUserId;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

}
