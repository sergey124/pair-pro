package org.vors.pairbot.service

import com.google.common.base.Joiner
import org.apache.commons.lang3.BooleanUtils
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
import org.vors.pairbot.model.EventStatus
import org.vors.pairbot.model.EventStatus.DECLINED
import org.vors.pairbot.model.EventStatus.NO_RESPONSE

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
        return if (participant.accepted != NO_RESPONSE || event.accepted == DECLINED) {
            removeKeyboardMarkup
        } else {
            getInviteKeyboard(event)
        }
    }

    fun getRemoveKeyboard(chatId: Long?, messageId: Int?): EditMessageReplyMarkup {
        return EditMessageReplyMarkup()
                .setChatId(chatId!!)
                .setMessageId(messageId)
                .setReplyMarkup(removeKeyboardMarkup)
    }

    private fun button(label: String, callbackData: String): InlineKeyboardButton {
        return callbackButton(label, callbackData)
    }

    private fun callbackButton(text: String, callbackData: String): InlineKeyboardButton {
        return InlineKeyboardButton()
                .setText(text)
                .setCallbackData(callbackData)
    }

    private fun createLinkButton(text: String, url: String): InlineKeyboardButton {
        return InlineKeyboardButton()
                .setText(text)
                .setUrl(url)
    }

    private fun getOneRowKeyboard(vararg buttons: InlineKeyboardButton): InlineKeyboardMarkup {
        return getMultiRowKeyboard(ArrayList(listOf(row(*buttons))))
    }

    private fun getMultiRowKeyboard(rows: List<List<InlineKeyboardButton>>): InlineKeyboardMarkup {
        return InlineKeyboardMarkup().setKeyboard(rows)
    }

    private fun row(vararg buttons: InlineKeyboardButton): List<InlineKeyboardButton> {
        return ArrayList(Arrays.asList(*buttons))
    }

    fun removeCustomKeyboard(): ReplyKeyboard {
        return ReplyKeyboardRemove()
    }
}
