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
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.vors.pairbot.constant.Callback;
import org.vors.pairbot.model.*;
import org.vors.pairbot.repository.EventRepository;
import org.vors.pairbot.repository.ParticipantRepository;
import org.vors.pairbot.repository.TeamRepository;
import org.vors.pairbot.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
            case ACCEPT_DECLINE:
                Long eventPk = Long.valueOf(callbackParts.get(1));

                Participant participant = participantRepository.getOne(new ParticipantId(user.getPk(), eventPk));

                boolean accepted = Boolean.valueOf(callbackParts.get(2));
                participant.setAccepted(accepted);
                participantRepository.save(participant);

                if (!accepted) {
                    user.setLastDeclineDate(new Date());
                    userRepository.save(user);
                }

                Event event = participant.getEvent();
                updateEvent(event, accepted);
                updateInvite(event);

                answerText = "ok";
                break;
            case VOID:
        }
        sendAnswerCallbackQuery(answerText, false, callbackquery);
    }

    private void updateEvent(Event event, boolean accepted) {
        List<Boolean> responses = event.getParticipants().stream()
                .map(Participant::isAccepted)
                .collect(Collectors.toList());

        if (!accepted) {
            event.setAccepted(false);
        } else if (responses.stream().allMatch(Boolean.TRUE::equals)) {
            event.setAccepted(true);
        }
        if (event.getAccepted() != null) {
            eventRepository.save(event);
        }
    }

    private void updateInvite(Event event) {
        messageService.updateToAll(
                event,
                messageService::pairDescriptionText,
                keyboardService::acceptedInviteKeyboard);
    }

    private Team newTeam(UserInfo user) {
        Team team = new Team();
        team.setToken(UUID.randomUUID());
        team.setCreator(user);
        team.addMember(user);
//        team.addMember(newDummyUser());

        teamRepository.save(team);
        return team;
    }

    /**
     * For testing when there's no second account
     */
    private UserInfo newDummyUser() {
        UserInfo user = new UserInfo();
        user.setUserId(0);
        user.setFirstName("Partner");
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
            messageService.sendMessage(chatId, messageService.getJoinTeamText(team));
        } catch (TelegramApiException e) {
            LOG.error("Sending join link failed", e);
        }
    }

}
