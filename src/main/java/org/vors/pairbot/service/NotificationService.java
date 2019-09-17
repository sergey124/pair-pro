package org.vors.pairbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.vors.pairbot.model.Event;
import org.vors.pairbot.model.Participant;
import org.vors.pairbot.model.UserInfo;
import org.vors.pairbot.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.Duration;
import java.util.List;

@Transactional
@Component
public class NotificationService {
    private Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserRepository userRepository;

    public void notifyAllUpcomingIn(Duration expiringIn, Duration scanPeriod) {
        LOG.info("Start notify about upcoming events");
        List<Event> events = userService.findUpcomingEvents(expiringIn, scanPeriod);
        LOG.info("Found {} people to notify", events.size());

        events.forEach(this::notifyUpcoming);

        LOG.info("End notify about upcoming events");
    }

    private void notifyUpcoming(Event event) {
        try {
            event.getParticipants().forEach(this::notifyUpcoming);
        } catch (Exception e) {
            LOG.error("Can't notify about upcoming event pk = {}\n{}", event.getPk(), e.toString(), e);
        }
    }

    private void notifyUpcoming(Participant participant) {
        UserInfo user = participant.getUser();
        Integer userId = user.getUserId();
        LOG.info("Notifying upcoming user {}", userId);
        try {
            Integer sentMessage = messageService.sendMessage(
                    messageService.getUpcomingNotificationMessage(participant));

            LOG.info("Notified upcoming user {}, id {}", user.getFirstName(), userId);

            user.setLastMessageId(sentMessage);

            userRepository.save(user);
        } catch (TelegramApiException e) {
            LOG.error("Can't notify expiring user {}\n{}", user.getPk(), e.toString(), e);
        }
    }

}
