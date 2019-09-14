package org.vors.pairbot.generator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.vors.pairbot.model.Event;
import org.vors.pairbot.model.Team;
import org.vors.pairbot.model.UserInfo;
import org.vors.pairbot.service.TimeService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.vors.pairbot.generator.PairGenerator.MIN_DAYS_BETWEEN_SESSIONS;

public class PairGeneratorTest {

    private PairGenerator systemUnderTest = new PairGenerator(new TimeService());
    private DateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
    private Date sessionDate;
    private Team team;
    private UserInfo user;
    private UserInfo member_noRecentEvents;
    private UserInfo member_recentEvent;

    public PairGeneratorTest() {

    }

    @Before
    public void setUp() throws Exception {
        sessionDate = format.parse("01-01-2000");
        Date oldSessionDate = datePlusDays(sessionDate, -MIN_DAYS_BETWEEN_SESSIONS - 1);
        Date recentSessionDate = datePlusDays(sessionDate, -MIN_DAYS_BETWEEN_SESSIONS);

        user = new UserInfo();
        member_noRecentEvents = newDummyUser(oldSessionDate);
        member_recentEvent = newDummyUser(recentSessionDate);

        team = new Team();
        team.addMember(user);
    }

    @Test
    public void givenPartnerHasNoRecentEvents_whenFindPair_thenFound() {
        //given
        team.addMember(member_noRecentEvents);

        //when
        Optional<Event> pair = systemUnderTest.findPair(user, sessionDate);

        //then
        UserInfo actual = pair.get().getPartner();
        Assert.assertEquals(member_noRecentEvents, actual);
    }

    @Test
    public void givenOneOfPartnersHasNoRecentEvents_whenFindPair_thenFindThisMember() {
        //given
        team.addMember(member_recentEvent);
        team.addMember(member_noRecentEvents);

        //when
        Optional<Event> pair = systemUnderTest.findPair(user, sessionDate);

        //then
        UserInfo actual = pair.get().getPartner();
        Assert.assertEquals(member_noRecentEvents, actual);
    }

    @Test
    public void givenAllPartnersHaveRecentEvent_whenFindPair_thenNotFound() {
        //given
        team.addMember(member_recentEvent);

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
        user.setLastDeclineDate(lastSessionDate);
        return user;
    }

    private Date datePlusDays(Date date, int days) {
        return Date.from(date.toInstant().plus(days, ChronoUnit.DAYS));
    }

}