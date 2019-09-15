package org.vors.pairbot.generator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.vors.pairbot.PairbotApplication;
import org.vors.pairbot.model.Event;
import org.vors.pairbot.model.Participant;
import org.vors.pairbot.model.Team;
import org.vors.pairbot.model.UserInfo;
import org.vors.pairbot.repository.EventRepository;
import org.vors.pairbot.repository.TeamRepository;
import org.vors.pairbot.repository.UserRepository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.vors.pairbot.generator.PairGenerator.MIN_DAYS_BETWEEN_SESSIONS;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PairbotApplication.class)
@Transactional
public class PairGeneratorTest {

    private DateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
    private Date sessionDate;
    private Team team;
    private UserInfo user;
    private UserInfo member_noRecentEvent;
    private UserInfo member_recentEvent;

    @Autowired
    private PairGenerator systemUnderTest;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;

    public PairGeneratorTest() {

    }

    @Before
    public void setUp() throws Exception {
        sessionDate = format.parse("01-01-2000");
        Date oldSessionDate = datePlusDays(sessionDate, -MIN_DAYS_BETWEEN_SESSIONS - 1);
        Date recentSessionDate = datePlusDays(sessionDate, -MIN_DAYS_BETWEEN_SESSIONS + 1);

        user = new UserInfo();
        member_noRecentEvent = newDummyUser(oldSessionDate);
        member_recentEvent = newDummyUser(recentSessionDate);

        Event event = new Event();
        event.addParticipant(new Participant(member_noRecentEvent));
        event.setDate(oldSessionDate);
        eventRepository.save(event);

        event = new Event();
        event.addParticipant(new Participant(member_recentEvent));
        event.setDate(recentSessionDate);
        eventRepository.save(event);

        team = new Team();
        team.addMember(user);

    }

    @Test
    public void givenPartnerHasNoRecentEvents_whenFindPair_thenFound() {
        //given
        team.addMember(member_noRecentEvent);
        teamRepository.save(team);

        //when
        Optional<Event> pair = systemUnderTest.findPair(user, sessionDate);

        //then
        UserInfo actual = pair.get().getPartner();
        Assert.assertEquals(member_noRecentEvent, actual);
    }

    @Test
    public void givenOneOfPartnersHasNoRecentEvents_whenFindPair_thenFindThisMember() {
        //given
        team.addMember(member_recentEvent);
        team.addMember(member_noRecentEvent);
        teamRepository.save(team);

        //when
        Optional<Event> pair = systemUnderTest.findPair(user, sessionDate);

        //then
        UserInfo actual = pair.get().getPartner();
        Assert.assertEquals(member_noRecentEvent, actual);
    }

    @Test
    public void givenAllPartnersHaveRecentEvent_whenFindPair_thenNotFound() {
        //given
        team.addMember(member_recentEvent);
        teamRepository.save(team);

        //when
        Optional<Event> pair = systemUnderTest.findPair(user, sessionDate);

        //then
        Assert.assertFalse(pair.isPresent());
    }


    private UserInfo newDummyUser(Date lastSessionDate) {
        UserInfo user = new UserInfo();
        int randomInt = ThreadLocalRandom.current().nextInt();
        user.setUserId(randomInt);
        user.setFirstName("" + randomInt);

        return user;
    }

    private Date datePlusDays(Date date, int days) {
        return Date.from(date.toInstant().plus(days, ChronoUnit.DAYS));
    }

}