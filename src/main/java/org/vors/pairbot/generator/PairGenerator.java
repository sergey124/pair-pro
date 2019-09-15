package org.vors.pairbot.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vors.pairbot.model.Event;
import org.vors.pairbot.model.Participant;
import org.vors.pairbot.model.UserInfo;
import org.vors.pairbot.repository.EventRepository;
import org.vors.pairbot.repository.ParticipantRepository;
import org.vors.pairbot.repository.UserRepository;
import org.vors.pairbot.service.TimeService;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class PairGenerator {
    static final int MIN_DAYS_BETWEEN_SESSIONS = 4;
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private TimeService timeService;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private ParticipantRepository participantRepository;
    @Autowired
    private UserRepository userRepository;

    public PairGenerator() {
    }

    public PairGenerator(TimeService timeService, EventRepository eventRepository, ParticipantRepository participantRepository, UserRepository userRepository) {
        this.timeService = timeService;
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
    }

    public Optional<Event> findPair(UserInfo user) {
        Date sessionDate = timeService.chooseSessionDate();
        return findPair(user, sessionDate);
    }

    public Optional<Event> findPair(UserInfo user, Date sessionDate) {
        List<UserInfo> others = findAvailablePeers(user, sessionDate);

        if (others.isEmpty()) {
            LOG.debug("Pair not found, no available peers");
            return Optional.empty();
        }
        Event event = pair(user, others);
        event.setDate(sessionDate);

        return Optional.of(event);
    }

    private List<UserInfo> findAvailablePeers(UserInfo user, Date date) {
        Date dateThreshold = timeService.beginningOfDateMinusDaysFrom(date, MIN_DAYS_BETWEEN_SESSIONS);

        return userRepository.findByNoEventsAfter(dateThreshold, user, user.getTeam());
    }

    private Event pair(UserInfo first, List<UserInfo> others) {

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int pairIndex = random.nextInt(others.size());
        UserInfo second = others.get(pairIndex);

        Event event = new Event();
        event.setCreator(first);
        event.setPartner(second);
        event.setCreatorHost(ThreadLocalRandom.current().nextBoolean());
        event.addParticipant(new Participant(first));
        event.addParticipant(new Participant(second));

        return event;
    }

    public boolean canCreateEvent(UserInfo user) {
        return !hasDeclinedRecently(user) && !hasUpcomingEvents(user);
    }

    private boolean hasUpcomingEvents(UserInfo user) {
        return eventRepository.existsByDateAfterAndParticipants_User(new Date(), user);
    }

    public boolean hasDeclinedRecently(UserInfo user) {
        Date lastDecline = user.getLastDeclineDate();
        return lastDecline != null && lastDecline.after(timeService.lastDeclineThreshold());
    }

}

