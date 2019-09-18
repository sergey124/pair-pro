package org.vors.pairbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.EntityType;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.vors.pairbot.generator.PairGenerator;
import org.vors.pairbot.model.Event;
import org.vors.pairbot.model.Team;
import org.vors.pairbot.model.UserInfo;
import org.vors.pairbot.repository.EventRepository;
import org.vors.pairbot.repository.TeamRepository;
import org.vors.pairbot.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.vors.pairbot.constant.BotCommands.*;


@Transactional
@Component
public class CommandService {
    private Logger LOG = LoggerFactory.getLogger(CommandService.class);

    @Autowired
    @Lazy
    private AbsSender bot;
    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;
    @Autowired
    private KeyboardService keyboardService;
    @Autowired
    private PairGenerator pairGenerator;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatService chatService;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private TeamRepository teamRepository;


    public void processCommand(Message message) throws TelegramApiException {
        Optional<String> commandTextOpt = extractCommandText(message);

        if (commandTextOpt.isPresent()) {
            String commandText = commandTextOpt.get();

            Long chatId = message.getChatId();
            UserInfo user = userService.findByUserId(message.getFrom().getId()).get();
            switch (commandText) {
                case START:
                    String messageText = message.getText();
                    if (commandText.length() < messageText.length()) {
                        String teamToken = messageText.substring(commandText.length() + 1);
                        joinTeamByToken(teamToken, user);

                        messageService.sendMessage(chatId, messageService.teamInfo(user));

                    } else {
                        SendMessage sendMessage = messageService.getMessageWithKeyboard(
                                chatId,
                                "Hi! Let's make a team. \nOr ask your peers for a link to join",
                                keyboardService.getStartKeyboard());
                        messageService.sendMessage(sendMessage);
                    }
                    break;
                case PAIR:
                    if (pairGenerator.canCreateEvent(user)) {
                        createEventAndInvite(user);
                    } else {
                        messageService.sendMessage(chatId, messageService.tryLaterText(user));
                    }
                    break;
                case MY_TEAM:
                    messageService.sendMessage(chatId, messageService.teamInfo(user));
                    break;
                case SET_LOCATION:
                    messageService.requestLocation(chatId);
                    break;

            }
        }
    }

    private void joinTeamByToken(String token, UserInfo user) {
        Optional<Team> teamOpt = teamRepository.findByToken(UUID.fromString(token));

        teamOpt.ifPresent(team -> {
            team.addMember(user);
            teamRepository.save(team);
        });
    }

    private void createEventAndInvite(UserInfo user) throws TelegramApiException {
        Optional<Event> eventOpt = pairGenerator.findPair(user);

        if (eventOpt.isPresent()) {
            Event event = eventOpt.get();
            eventRepository.save(event);

            messageService.sendToAll(event, messageService::inviteText, keyboardService::getInviteKeyboard);

        } else {
            sendNoPairsAvailable(user);
        }
    }



    private void sendNoPairsAvailable(UserInfo user) throws TelegramApiException {
        SendMessage message = messageService.getMessage(
                chatService.getPrivateChatId(user),
                "No pairs available.\nEveryone is busy or no one joined yet \uD83D\uDE1E"
        );
        messageService.sendMessage(message);
    }

    private Optional<String> extractCommandText(Message message) {
        Optional<List<MessageEntity>> entitiesOpt = Optional.ofNullable(message.getEntities());

        return entitiesOpt.flatMap(this::extractCommandText);
    }

    private Optional<String> extractCommandText(List<MessageEntity> entities) {
        return entities.stream()
                .filter(e -> e != null && e.getOffset() == 0 && EntityType.BOTCOMMAND.equals(e.getType()))
                .findFirst()
                .map(MessageEntity::getText)
                .map(this::removeMention);
    }

    private String removeMention(String commandText) {
        int indexOfMention = commandText.indexOf("@");
        if (indexOfMention != -1) {
            commandText = commandText.substring(0, indexOfMention);
        }
        return commandText;
    }

}
