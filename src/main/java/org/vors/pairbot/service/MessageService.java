package org.vors.pairbot.service;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.ocpsoft.pretty.time.PrettyTime;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.vors.pairbot.generator.PairGenerator;
import org.vors.pairbot.model.Event;
import org.vors.pairbot.model.Participant;
import org.vors.pairbot.model.Team;
import org.vors.pairbot.model.UserInfo;
import org.vors.pairbot.repository.ParticipantRepository;
import org.vors.pairbot.repository.UserRepository;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class MessageService {
    public static final int MAX_TEXT_MESSAGE_LENGTH = 4095;
    private static final String PAIR_DESCRIPTION_TEMPLATE = "pair_description.ftl";

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private PrettyTime prettyTime = new PrettyTime();

    @Value("${bot.url}")
    private String botUrl;

    @Autowired
    @Lazy
    private AbsSender bot;
    @Autowired
    private KeyboardService keyboardService;
    @Autowired
    private UserService userService;
    @Autowired
    private Configuration freemarkerConfig;
    @Autowired
    private TimeService timeService;
    @Autowired
    private PairGenerator pairGenerator;
    @Autowired
    private ChatService chatService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ParticipantRepository participantRepository;

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

    public String inviteText(UserInfo user, Event pair) {
        String pairDescription = pairDescriptionText(user, pair);

        return String.format("How about this session?\n\n%s", pairDescription);
    }

    public String pairDescriptionText(UserInfo user, Event event) {
        Map<String, Object> ctx = new HashMap<>();

        ctx.put("date", event.getDate());
        ctx.put("accepted", event.getAccepted());

        UserInfo creator = event.getCreator();
        UserInfo partner = event.getPartner();
        Boolean creatorOk = isAccepted(event, creator);
        Boolean partnerOk = isAccepted(event, partner);
        boolean pendingOther = user.equals(partner) && Boolean.TRUE.equals(partnerOk) && creatorOk == null
                || user.equals(creator) && Boolean.TRUE.equals(creatorOk) && partnerOk == null;

        ctx.put("pendingOther", pendingOther);
        ctx.put("creatorLink", userLink(creator));
        ctx.put("partnerLink", userLink(partner));
        ctx.put("creatorOk", creatorOk);
        ctx.put("partnerOk", partnerOk);
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

    public SendMessage getUpcomingNotificationMessage(Participant participant) {
        UserInfo user = participant.getUser();
        Long chatId = chatService.getPrivateChatId(user);
        String text = getUpcomingNotificationText(participant);
        return getMessage(chatId, text);
    }

    private String getUpcomingNotificationText(Participant participant) {
        UserInfo user = participant.getUser();
        Event event = participant.getEvent();
        return "Upcoming session in " + prettyTime.format(event.getDate()) + ":\n\n" + pairDescriptionText(user, event);
    }

    public String tryLaterText(UserInfo user) throws TelegramApiException {
        if (pairGenerator.hasDeclinedRecently(user)) {
            return "To make sure the choice is random, everyone has one shot.\nNext try is available in " +
                    prettyTime.format(timeService.nextDateToCreateEvent(user));
        } else {
            List<Participant> upcomingParticipants = participantRepository.getParticipantsAfter(new Date(), user);
            if (!upcomingParticipants.isEmpty()) {
                return getUpcomingNotificationText(upcomingParticipants.get(0));
            } else {
                return "Pair already created";
            }
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

    public void sendToAll(Event event, Maps.EntryTransformer<UserInfo, Event, String> textProvider, Function<Event, InlineKeyboardMarkup> keyboardProvider) {
        InlineKeyboardMarkup keyboard = keyboardProvider.apply(event);

        for (Participant p : event.getParticipants()) {
            UserInfo user = p.getUser();
            String text = textProvider.transformEntry(user, event);
            try {
                SendMessage message = getMessageWithKeyboard(
                        chatService.getPrivateChatId(user),
                        text,
                        keyboard);
                user.setLastMessageId(sendMessage(message));
                userRepository.save(user);
            } catch (TelegramApiException e) {
                LOG.error("Sending failed: {}", e.toString(), e);
            }
        }
    }

    public void updateToAll(Event event, Maps.EntryTransformer<UserInfo, Event, String> textProvider, Maps.EntryTransformer<Participant, Event, InlineKeyboardMarkup> keyboardProvider) {

        for (Participant p : event.getParticipants()) {
            UserInfo user = p.getUser();
            String text = textProvider.transformEntry(user, event);
            InlineKeyboardMarkup keyboard = keyboardProvider.transformEntry(p, event);
            try {
                editMessage(
                        chatService.getPrivateChatId(user),
                        user.getLastMessageId(),
                        text,
                        keyboard);
            } catch (TelegramApiException e) {
                LOG.error("Sending failed: {}", e.toString(), e);
            }
        }
    }

    public String teamInfo(UserInfo user) {
        Team team = user.getTeam();
        if (team != null) {
            String teamList = team.getMembers().stream()
                    .map(this::userLink)
                    .collect(Collectors.joining("\n"));
            String teamPart;
            if (user.equals(team.getCreator())) {
                teamPart = inlineLink("team", teamLink(team));
            } else {
                teamPart = "team";
            }
            return "Your " + teamPart + ":\n" + teamList;
        } else {
            return "You have no team";
        }
    }

    public String getJoinTeamText(Team team) {
        return "Your team created!\n"
                + inlineLink("Right-click to copy link", teamLink(team)) + "\n\nAfter someone joins, use /pair command";
    }

    public String teamLink(Team team) {
        return botUrl + "?start=" + team.getToken();
    }

    private String inlineLink(String label, String link) {
        return String.format("[%s](%s)", label, link);
    }
}
