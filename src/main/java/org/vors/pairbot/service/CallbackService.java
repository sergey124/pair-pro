package org.vors.pairbot.service;

import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.vors.pairbot.constant.Callback;
import org.vors.pairbot.model.*;
import org.vors.pairbot.repository.EventRepository;
import org.vors.pairbot.repository.ParticipantRepository;
import org.vors.pairbot.repository.TeamRepository;
import org.vors.pairbot.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.vors.pairbot.constant.BotConstants.CALLBACK_DATA_SEPARATOR;

@Transactional
@Component
public class CallbackService {
    private Logger LOG = LoggerFactory.getLogger(CommandService.class);

    @Autowired
    @Lazy
    private AbsSender bot;
    @Autowired
    private UserService userService;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private KeyboardService keyboardService;
    @Autowired
    private ParticipantRepository participantRepository;


    public void processKeyboardCallback(CallbackQuery callbackquery) throws TelegramApiException {
        List<String> callbackParts = extractCallbackParts(callbackquery);
        if (CollectionUtils.isEmpty(callbackParts)) {
            return;
        }

        Integer userId = callbackquery.getFrom().getId();

        Optional<UserInfo> userOpt = userService.findByUserId(userId);

        if (!userOpt.isPresent()) {
            return;
        }

        String answerText = null;
        UserInfo user = userOpt.get();
        Long chatId = callbackquery.getMessage().getChatId();

        Callback callback = Callback.valueOf(callbackParts.get(0));
        switch (callback) {
            case NEW_TEAM:
                Team team = newTeam(user);
                sendJoinLink(chatId, team);
                break;
            case ADD_TO_TEAM:
                answerText = "ask your peers for a link";
                break;
            case CONFIRM:
                Long eventPk = Long.valueOf(callbackParts.get(1));

                Participant participant = participantRepository.getOne(new ParticipantId(user.getPk(), eventPk));
                participant.setAccepted(true);

                participantRepository.save(participant);

                hideKeyboardAndUpdateConfirmed(callbackquery, participant.getEvent());

                answerText = "confirmed";
                break;
            case VOID:
        }
        sendAnswerCallbackQuery(answerText, false, callbackquery);
    }

    private void hideKeyboardAndUpdateConfirmed(CallbackQuery callbackquery, Event pair) throws TelegramApiException {
        Message original = callbackquery.getMessage();
        InlineKeyboardMarkup removeKeyboard = keyboardService.getRemoveKeyboardMarkup();

        String text = messageService.pairDescription(pair);

        messageService.editMessage(original.getChatId(), original.getMessageId(), text, removeKeyboard);
    }


    private Team newTeam(UserInfo user) {
        Team team = new Team();
//        team.setMembers(Sets.newHashSet(user));
        team.addMember(user);
        team.addMember(newDummyUser(user));

        teamRepository.save(team);
        return team;
    }

    private UserInfo newDummyUser(UserInfo source) {
        UserInfo user = new UserInfo();
        user.setUserId(0);
        user.setFirstName("Dummy " + source.getFirstName());
        userRepository.save(user);
        return user;
    }


    private void sendAnswerCallbackQuery(String text, boolean alert, CallbackQuery callbackquery) throws TelegramApiException {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackquery.getId());
        answerCallbackQuery.setShowAlert(alert);
        answerCallbackQuery.setText(text);
        bot.execute(answerCallbackQuery);
    }

    private List<String> extractCallbackParts(CallbackQuery callbackquery) {
        String callbackData = callbackquery.getData();

        return Splitter.on(CALLBACK_DATA_SEPARATOR).splitToList(callbackData);
    }

    private void sendJoinLink(Long chatId, Team team) {
        try {
            messageService.sendMessage(chatId, "Your team created!\n Here's a join link for your peers: "
                    + "https://t.me/pprobot?start=" + team.getPk());
        } catch (TelegramApiException e) {
            LOG.error("Sending join link failed", e);
        }
    }
}
