package org.vors.pairbot.service;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.vors.pairbot.constant.Callback;
import org.vors.pairbot.model.Event;
import org.vors.pairbot.model.Participant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.vors.pairbot.constant.BotConstants.CALLBACK_DATA_SEPARATOR;

@Component
public class KeyboardService {
    public InlineKeyboardMarkup getStartKeyboard() {
        return getOneRowKeyboard(button("New team", Callback.NEW_TEAM.toString()));
    }

    public InlineKeyboardMarkup getInviteKeyboard(Event event) {
        return getOneRowKeyboard(
                button(
                        "Decline",
                        Joiner.on(CALLBACK_DATA_SEPARATOR).join(Callback.ACCEPT_DECLINE.toString(), event.getPk(), Boolean.FALSE)
                ),
                button(
                        "Accept",
                        Joiner.on(CALLBACK_DATA_SEPARATOR).join(Callback.ACCEPT_DECLINE.toString(), event.getPk(), Boolean.TRUE)
                ));
    }

    public InlineKeyboardMarkup acceptedInviteKeyboard(Participant participant, Event event) {
        if (participant.isAccepted() != null || BooleanUtils.isFalse(event.getAccepted())) {
            return getRemoveKeyboardMarkup();
        } else {
            return getInviteKeyboard(event);
        }
    }

    public EditMessageReplyMarkup getRemoveKeyboard(Long chatId, Integer messageId) {
        return new EditMessageReplyMarkup()
                .setChatId(chatId)
                .setMessageId(messageId)
                .setReplyMarkup(getRemoveKeyboardMarkup());
    }

    public InlineKeyboardMarkup getRemoveKeyboardMarkup() {
        return getOneRowKeyboard();
    }

    private InlineKeyboardButton button(String label, String callbackData) {
        return callbackButton(label, callbackData);
    }

    private InlineKeyboardButton callbackButton(String text, String callbackData) {
        return new InlineKeyboardButton()
                .setText(text)
                .setCallbackData(callbackData);
    }

    private InlineKeyboardButton createLinkButton(String text, String url) {
        return new InlineKeyboardButton()
                .setText(text)
                .setUrl(url);
    }

    private InlineKeyboardMarkup getOneRowKeyboard(InlineKeyboardButton... buttons) {
        return getMultiRowKeyboard(new ArrayList<>(Collections.singletonList(row(buttons))));
    }

    private InlineKeyboardMarkup getMultiRowKeyboard(List<List<InlineKeyboardButton>> rows) {
        return new InlineKeyboardMarkup().setKeyboard(rows);
    }

    private List<InlineKeyboardButton> row(InlineKeyboardButton... buttons) {
        return new ArrayList<>(Arrays.asList(buttons));
    }

}
