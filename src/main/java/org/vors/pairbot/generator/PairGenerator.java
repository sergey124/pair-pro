package org.vors.pairbot.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vors.pairbot.model.Event;
import org.vors.pairbot.model.Participant;
import org.vors.pairbot.model.Team;
import org.vors.pairbot.model.UserInfo;
import org.vors.pairbot.service.TimeService;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class PairGenerator {
    static final int MIN_DAYS_BETWEEN_SESSIONS = 4;
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private TimeService timeService;

    public PairGenerator() {
    }

    public PairGenerator(TimeService timeService) {
        this.timeService = timeService;
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
        Team team = user.getTeam();

        return team.getMembers().stream()
                .filter(member -> !member.equals(user) && isLastSessionLongBefore(date, member))
                .collect(Collectors.toList());
    }

    private boolean isLastSessionLongBefore(Date date, UserInfo member) {
        return member.getLastPairDate() == null || member.getLastPairDate().before(timeService.beginningOfDateMinusDaysFrom(date, MIN_DAYS_BETWEEN_SESSIONS));
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

}

