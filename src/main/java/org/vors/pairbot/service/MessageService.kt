package org.vors.pairbot.service

import com.ocpsoft.pretty.time.PrettyTime
import freemarker.template.Configuration
import freemarker.template.TemplateException
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.vors.pairbot.model.*
import org.vors.pairbot.model.ParticipantStatus.*
import org.vors.pairbot.repository.ParticipantRepository
import org.vors.pairbot.repository.UserRepository
import java.io.IOException
import java.io.Serializable
import java.io.StringWriter
import java.time.ZoneId
import java.util.*
import java.util.Comparator.nullsFirst
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2


@Component
open class MessageService(
        @Value("\${bot.url}")
        var botUrl: String,
        @Lazy
        var bot: AbsSender,
        var keyboardService: KeyboardService,
        var userService: UserService,
        var freemarkerConfig: Configuration,
        var timeService: TimeService,
        var chatService: ChatService,
        var userRepository: UserRepository,
        var participantRepository: ParticipantRepository,
        var gameService: GameService
) {
    companion object {
        const val MAX_TEXT_MESSAGE_LENGTH = 4095
        const val PAIR_DESCRIPTION_TEMPLATE = "pair_description.ftl"
    }

    private val LOG = LoggerFactory.getLogger(javaClass)
    private val prettyTime = PrettyTime()

    @Throws(TelegramApiException::class)
    open fun sendMessage(chatId: Long, text: String): Int {
        return sendMessage(getMessage(chatId, truncateToMaxMessageLength(text)))
    }

    @Throws(TelegramApiException::class)
    open fun sendMessage(sendMessage: SendMessage): Int {
        return Optional.ofNullable(bot.execute(sendMessage))
                .map { it.messageId }
                .orElse(null)
    }

    @Throws(TelegramApiException::class)
    open fun sendMessage(chatId: Long, text: String, keyboard: ReplyKeyboard): Int {
        return sendMessage(getMessageWithKeyboard(chatId, text, keyboard, ParseMode.MARKDOWN))
    }

    fun getMessage(chatId: Long, text: String): SendMessage {
        val sendMessage = SendMessage()
        sendMessage.enableMarkdown(true)
        sendMessage.disableNotification()
        sendMessage.setChatId(chatId.toString())
        sendMessage.text = text

        return sendMessage
    }

    @JvmOverloads
    fun getMessageWithKeyboard(chatId: Long, text: String, keyboard: ReplyKeyboard, parseMode: String): SendMessage {
        val sendMessage = SendMessage()
        sendMessage.enableMarkdown(true)
        sendMessage.replyMarkup = keyboard
        sendMessage.setParseMode(parseMode)
        sendMessage.disableNotification()
        sendMessage.setChatId(chatId.toString())
        sendMessage.text = text
        sendMessage.disableWebPagePreview()

        return sendMessage
    }

    @Throws(TelegramApiException::class)
    fun editMessage(chatId: Long, messageId: Int, text: String, keyboard: InlineKeyboardMarkup): Serializable {
        val messageToEdit = getEditMessage(chatId, messageId, text, keyboard)

        return bot.execute(messageToEdit)
    }

    fun getEditMessage(chatId: Long, messageId: Int, text: String, keyboard: InlineKeyboardMarkup): EditMessageText {
        val messageToEdit = EditMessageText()
        messageToEdit.setChatId(chatId.toString())
        messageToEdit.messageId = messageId
        messageToEdit.text = text
        messageToEdit.replyMarkup = keyboard
        messageToEdit.setParseMode(ParseMode.MARKDOWN)
        messageToEdit.disableWebPagePreview()
        return messageToEdit
    }


    fun truncateToMaxMessageLength(text: String): String {
        return StringUtils.abbreviate(text, MAX_TEXT_MESSAGE_LENGTH)
    }

    fun inviteText(user: UserInfo, pair: Event): String {
        val pairDescription = pairDescriptionText(user, pair)

        return String.format("How about this session?\n\n%s", pairDescription)
    }

    fun pairDescriptionText(user: UserInfo, event: Event): String {
        val ctx = HashMap<String, Any>()

        var current = event.participants.first { it.user == user }
        var other = event.participants.first { it.user != user }

        ctx["status"] = eventStatus(current, other).toString().replace('_', ' ')

        val instant = event.date.toInstant()
        val zone = chooseTimezone(current.user, other.user)

        ctx["date"] = instant.atZone(zone)
        ctx["zone"] = zone.toString()

        ctx["thisLink"] = userLink(current.user)
        ctx["otherLink"] = userLink(other.user)

        ctx["thisHost"] = !(event.creatorHost xor (event.creator == current.user))

        try {
            val stringWriter = StringWriter()
            freemarkerConfig.getTemplate(PAIR_DESCRIPTION_TEMPLATE).process(ctx, stringWriter)

            return stringWriter.toString()
        } catch (e: IOException) {
            LOG.error("Can't construct description from template", e)
            throw RuntimeException(e)
        } catch (e: TemplateException) {
            LOG.error("Can't construct description from template", e)
            throw RuntimeException(e)
        }

    }

    private fun eventStatus(current: Participant, other: Participant): EventStatus {
        val curStat = current.status
        val otherStat = other.status

        val eventStatus = when {
            curStat == NO_RESPONSE && otherStat == NO_RESPONSE -> EventStatus.NO_RESPONSE
            curStat == DECLINED || otherStat == DECLINED -> EventStatus.CANCELLED
            curStat == NO_RESPONSE && otherStat == ACCEPTED -> EventStatus.WAITING_YOU
            curStat == ACCEPTED && otherStat == NO_RESPONSE -> EventStatus.WAITING_PARTNER
            curStat == ACCEPTED && otherStat == ACCEPTED -> EventStatus.CONFIRMED
            else -> throw IllegalStateException("Unsupported combination of participant statuses")
        }
        return eventStatus
    }

    private fun other(user: UserInfo, event: Event): UserInfo {
        return event.participants.first { it.user != user }.user
    }

    private fun chooseTimezone(creator: UserInfo, partner: UserInfo): ZoneId {
        val zone: ZoneId
        if (creator.timezone == null && partner.timezone == null) {
            zone = ZoneId.of("UTC")
        } else if (creator.timezone == null) {
            zone = partner.timezone!!
        } else {
            zone = creator.timezone!!
        }
        return zone
    }

    fun getUpcomingNotificationMessage(participant: Participant): SendMessage {
        val user = participant.user
        val chatId = chatService.getPrivateChatId(user)
        val text = getUpcomingNotificationText(participant)
        return getMessage(chatId, text)
    }

    private fun getUpcomingNotificationText(participant: Participant): String {
        val user = participant.user
        val event = participant.event
        return "Upcoming session in " + prettyTime.format(event.date) + ":\n\n" + pairDescriptionText(user, event)
    }

    @Throws(TelegramApiException::class)
    fun tryLaterText(user: UserInfo, hasDeclinedRecently: Boolean): String {
        return if (hasDeclinedRecently) {
            "To make sure the choice is random, everyone has one shot.\nNext try is available in " +
                    prettyTime.format(timeService.nextDateToCreateEvent(user))
        } else {
            val upcomingParticipants = participantRepository.getParticipantsAfter(Date(), user)
            if (upcomingParticipants.isNotEmpty()) {
                getUpcomingNotificationText(upcomingParticipants[0])
            } else {
                "Pair already created"
            }
        }

    }

    fun userLink(user: UserInfo): String {
        return userMentionText(user)
    }

    fun userMentionText(user: UserInfo): String {
        val label = Optional.ofNullable(user.firstName).orElse("Noname")
        return String.format("[%s](tg://user?id=%s)", label, user.userId)
    }

    fun sendToAll(event: Event, textProvider: KFunction2<@ParameterName(name = "user") UserInfo, @ParameterName(name = "pair") Event, String>, keyboardProvider: KFunction1<@ParameterName(name = "event") Event, InlineKeyboardMarkup>) {
        val keyboard = keyboardProvider.invoke(event)

        for (p in event.participants) {
            val user = p.user
            val text = textProvider.invoke(user, event)
            try {
                val message = getMessageWithKeyboard(
                        chatService.getPrivateChatId(user),
                        text,
                        keyboard,
                        ParseMode.MARKDOWN)
                user.lastMessageId = sendMessage(message)
                userRepository.save(user)
            } catch (e: TelegramApiException) {
                LOG.error("Sending failed: {}", e.toString(), e)
            }

        }
    }

    fun updateToAll(event: Event, textProvider: Function2<UserInfo, Event, String>, keyboardProvider: Function2<Participant, Event, InlineKeyboardMarkup>) {

        for (p in event.participants) {
            val user = p.user
            val text = textProvider.invoke(user, event)
            val keyboard = keyboardProvider.invoke(p, event)
            try {
                user.lastMessageId?.let {
                    editMessage(
                            chatService.getPrivateChatId(user),
                            it,
                            text,
                            keyboard)
                }
            } catch (e: TelegramApiException) {
                LOG.error("Sending failed: {}", e.toString(), e)
            }

        }
    }

    fun teamInfo(user: UserInfo): String {
        val team = user.team
        if (team != null) {
            val teamList = team.members
                    .sortedWith(nullsFirst(compareBy(UserInfo::firstName)))
                    .joinToString(separator = "\n") { this.userLine(it) }

            val teamPart = inlineLink("team", teamLink(team))

            return "Your $teamPart:\n$teamList"
        } else {
            return "You have no team"
        }
    }

    private fun userLine(user: UserInfo): String {
        return gameService.xpShort(user) + " " + userLink(user)
    }

    fun getJoinTeamText(team: Team): String {
        return ("Your team created!\n"
                + inlineLink("Right-click to copy link", teamLink(team)) + "\n\nAfter someone joins, use /pair command")
    }

    fun teamLink(team: Team): String {
        return botUrl + "?start=" + team.token
    }

    private fun inlineLink(label: String, link: String): String {
        return String.format("[%s](%s)", label, link)
    }

    @Throws(TelegramApiException::class)
    fun requestLocation(chatId: Long?) {
        val sendMessage = SendMessage(chatId!!.toString(), "Share location")
        sendMessage.replyMarkup = requestLocationKeyboard()

        val message = bot.execute(sendMessage)

        val location = message.location
        if (location != null) {
            LOG.info("Location acquired: lat {} , long {}", location.latitude, location.longitude)
        }

    }

    private fun requestLocationKeyboard(): ReplyKeyboard {
        val keyboardMarkup = ReplyKeyboardMarkup()
        keyboardMarkup.resizeKeyboard = true
        keyboardMarkup.oneTimeKeyboard = true
        keyboardMarkup.selective = true
        val keyboard = ArrayList<KeyboardRow>()
        val row = KeyboardRow()
        val button = KeyboardButton()
        button.text = "Share location"
        button.requestLocation = true
        row.add(button)
        keyboard.add(row)
        keyboardMarkup.keyboard = keyboard
        return keyboardMarkup
    }
}
