package org.vors.pairbot.service

import com.google.common.base.Joiner
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.vors.pairbot.constant.Callback
import org.vors.pairbot.model.Event
import org.vors.pairbot.model.Participant

import java.util.ArrayList
import java.util.Arrays

import org.vors.pairbot.constant.BotConstants.CALLBACK_DATA_SEPARATOR
import org.vors.pairbot.model.ParticipantStatus.DECLINED
import org.vors.pairbot.model.ParticipantStatus.NO_RESPONSE

@Component
class KeyboardService {
    val startKeyboard: InlineKeyboardMarkup
        get() = getOneRowKeyboard(button("New team", Callback.NEW_TEAM.toString()))

    val removeKeyboardMarkup: InlineKeyboardMarkup
        get() = getOneRowKeyboard()

    fun getInviteKeyboard(event: Event): InlineKeyboardMarkup {
        return getOneRowKeyboard(
                button(
                        "Decline",
                        Joiner.on(CALLBACK_DATA_SEPARATOR).join(Callback.ACCEPT_DECLINE.toString(), event.pk, java.lang.Boolean.FALSE)
                ),
                button(
                        "Accept",
                        Joiner.on(CALLBACK_DATA_SEPARATOR).join(Callback.ACCEPT_DECLINE.toString(), event.pk, java.lang.Boolean.TRUE)
                ))
    }

    fun acceptedInviteKeyboard(participant: Participant, event: Event): InlineKeyboardMarkup {
        return if (participant.status != NO_RESPONSE || cancelled(event)) {
            removeKeyboardMarkup
        } else {
            getInviteKeyboard(event)
        }
    }

    private fun cancelled(event: Event): Boolean {
        return event.participants.any { it.status == DECLINED }
    }

    fun getRemoveKeyboard(chatId: String?, messageId: Int?): EditMessageReplyMarkup {
        return EditMessageReplyMarkup.builder()
                .chatId(chatId!!)
                .messageId(messageId)
                .replyMarkup(removeKeyboardMarkup)
                .build()
    }

    private fun button(label: String, callbackData: String): InlineKeyboardButton {
        return callbackButton(label, callbackData)
    }

    private fun callbackButton(text: String, callbackData: String): InlineKeyboardButton {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build()
    }

    private fun createLinkButton(text: String, url: String): InlineKeyboardButton {
        return InlineKeyboardButton.builder()
                .text(text)
                .url(url)
                .build()
    }

    private fun getOneRowKeyboard(vararg buttons: InlineKeyboardButton): InlineKeyboardMarkup {
        return getMultiRowKeyboard(ArrayList(listOf(row(*buttons))))
    }

    private fun getMultiRowKeyboard(rows: List<List<InlineKeyboardButton>>): InlineKeyboardMarkup {
        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build()
    }

    private fun row(vararg buttons: InlineKeyboardButton): List<InlineKeyboardButton> {
        return ArrayList(Arrays.asList(*buttons))
    }

    fun removeCustomKeyboard(): ReplyKeyboard {
        return ReplyKeyboardRemove()
    }
}
