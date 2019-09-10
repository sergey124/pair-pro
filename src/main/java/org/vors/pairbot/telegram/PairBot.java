package org.vors.pairbot.telegram;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.vors.pairbot.constant.ChatUpdateHandlerFlow;
import org.vors.pairbot.repository.TeamRepository;
import org.vors.pairbot.service.CommandService;
import org.vors.pairbot.service.UserService;
import org.vors.pairbot.telegram.handler.ChatUpdateHandler;
import org.vors.pairbot.telegram.handler.impl.CommandHandler;

import static org.vors.pairbot.constant.ChatUpdateHandlerFlow.*;


@Component
public class PairBot extends TelegramLongPollingBot {
    private Logger LOG = LoggerFactory.getLogger(PairBot.class);
    @Value("${bot.token}")
    private String botToken;
    @Value("${bot.username}")
    private String botUsername;
    private Integer botUserId = null;

    @Autowired
    private ChatUpdateHandler callbackHandler;
    @Autowired
    private CommandService commandService;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private CommandHandler commandHandler;

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

        ChatUpdateHandlerFlow handlerFlow = chooseHandlerFlow(update);

        User currentUser = resolveCurrentUser(update, handlerFlow);
        if (isNew(currentUser)) {
            userService.createAndSaveUser(currentUser);
        }

        switch (handlerFlow) {
            case CALLBACK:
                try {
                    callCallbackHandlers(update);
                } catch (TelegramApiException e) {
                    LOG.error("Callback processing failed", e);
                }
                break;
            case COMMAND:
                try {
                    callCommandHandlers(update);
                } catch (TelegramApiException e) {
                    LOG.error("Command processing failed", e);
                }
                break;
        }
    }

    private boolean isNew(User currentUser) {
        return !userService.findByUserId(currentUser.getId()).isPresent();
    }


    private User resolveCurrentUser(Update update, ChatUpdateHandlerFlow handlerFlow) {
        switch (handlerFlow) {
            case CALLBACK:
                return update.getCallbackQuery().getFrom();
            case MEMBER_REMOVED:
                return update.getMessage().getLeftChatMember();
            case COMMAND:
                return update.getMessage().getFrom();
            default:
                return null;
        }
    }

    private void callCallbackHandlers(Update update) throws TelegramApiException {
        callbackHandler.handle(update);
    }

    private void callCommandHandlers(Update update) throws TelegramApiException {
        commandHandler.handle(update);
    }

    private ChatUpdateHandlerFlow chooseHandlerFlow(Update update) {
        if (update.hasCallbackQuery()) {
            return CALLBACK;
        }
        if (isCommand(update)) {
            return COMMAND;
        }

        Message message = update.getMessage();
        Preconditions.checkNotNull(message);

        if (message.getLeftChatMember() != null) {
            return MEMBER_REMOVED;
        }
        if (message.getChat().isUserChat()) {
            return CHAT;
        }
        return VOID;
    }

    private boolean isCommand(Update update) {
        Message message = update.getMessage();
        return message != null && message.isCommand();
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
