package org.vors.pairbot.service;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.vors.pairbot.model.Event;
import org.vors.pairbot.model.UserInfo;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Component
public class MessageService {
    public static final int MAX_TEXT_MESSAGE_LENGTH = 4095;
    private static final String PAIR_DESCRIPTION_TEMPLATE = "pair_description.ftl";

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    @Autowired
    @Lazy
    private AbsSender bot;
    @Autowired
    private KeyboardService keyboardService;
    @Autowired
    private UserService userService;
    @Autowired
    private Configuration freemarkerConfig;

    public Integer sendMessage(Long chatId, String text) throws TelegramApiException {
        return sendMessage(getMessage(chatId, truncateToMaxMessageLength(text)));
    }

    public Integer sendMessage(SendMessage sendMessage) throws TelegramApiException {

        return Optional.ofNullable(bot.execute(sendMessage))
                .map(Message::getMessageId)
                .orElse(null);
    }

    public SendMessage getMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.disableNotification();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        return sendMessage;
    }

    public SendMessage getMessageWithKeyboard(Long chatId, String text, ReplyKeyboard keyboard) {
        return getMessageWithKeyboard(chatId, text, keyboard, ParseMode.MARKDOWN);
    }

    public SendMessage getMessageWithKeyboard(Long chatId, String text, ReplyKeyboard keyboard, String parseMode) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(keyboard);
        sendMessage.setParseMode(parseMode);
        sendMessage.disableNotification();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.disableWebPagePreview();

        return sendMessage;
    }

    public Serializable editMessage(Long chatId, Integer messageId, String text)
            throws TelegramApiException {
        return editMessage(chatId, messageId, text, null);
    }

    public Serializable editMessage(Long chatId, Integer messageId, String text, InlineKeyboardMarkup keyboard)
            throws TelegramApiException {
        EditMessageText messageToEdit = getEditMessage(chatId, messageId, text, keyboard);

        return bot.execute(messageToEdit);
    }

    public EditMessageText getEditMessage(Long chatId, Integer messageId, String text, InlineKeyboardMarkup keyboard) {
        EditMessageText messageToEdit = new EditMessageText();
        messageToEdit.setChatId(chatId);
        messageToEdit.setMessageId(messageId);
        messageToEdit.setText(text);
        messageToEdit.setReplyMarkup(keyboard);
        messageToEdit.setParseMode(ParseMode.MARKDOWN);
        messageToEdit.disableWebPagePreview();
        return messageToEdit;
    }


    public String truncateToMaxMessageLength(String text) {
        return StringUtils.abbreviate(text, MAX_TEXT_MESSAGE_LENGTH);
    }

    public String pairDescription(Event event) {
        Map<String, Object> ctx = new HashMap<>();

        ctx.put("date", event.getDate());

        UserInfo creator = event.getCreator();
        UserInfo partner = event.getPartner();

        ctx.put("creatorLink", userLink(creator));
        ctx.put("partnerLink", userLink(partner));
        ctx.put("creatorOk", isAccepted(event, creator));
        ctx.put("partnerOk", isAccepted(event, partner));
        ctx.put("creatorHost", event.isCreatorHost());

        try {
            StringWriter stringWriter = new StringWriter();
            freemarkerConfig.getTemplate(PAIR_DESCRIPTION_TEMPLATE).process(ctx, stringWriter);

            return stringWriter.toString();
        } catch (IOException | TemplateException e) {
            LOG.error("Can't construct description from template", e);
            throw new RuntimeException(e);
        }
    }

    private Boolean isAccepted(Event event, UserInfo creator) {
        return event.getParticipants().stream()
                .filter(p -> p.getUser().equals(creator))
                .findAny().get().isAccepted();
    }

    public String userLink(UserInfo user) {
        return userMentionText(user);
    }

    private String addHostMark(String firstLabel) {
        return "\\*" + firstLabel;
    }

    public String userMentionText(UserInfo user) {
        return String.format("[%s](tg://user?id=%s)", user.getFirstName(), user.getUserId());
    }

}
