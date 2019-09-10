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

    private static final String FIRST_SESSION_DATE_STRING = "01-01-2000";

    private PairGenerator systemUnderTest = new PairGenerator(new TimeService());
    private UserInfo user;
    private Date sessionDate;
    private DateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
    private UserInfo firstPeer;

    public PairGeneratorTest() {

    }

    @Before
    public void setUp() throws Exception {
        Date firstSessionDate = format.parse(FIRST_SESSION_DATE_STRING);

        firstPeer = newDummyUser(firstSessionDate);
        sessionDate = datePlusDays(firstSessionDate, MIN_DAYS_BETWEEN_SESSIONS + 1);
        user = new UserInfo();

        Team team = new Team();
        team.addMember(user);
        team.addMember(firstPeer);
        team.addMember(newDummyUser(datePlusDays(firstSessionDate, 1)));
        team.addMember(newDummyUser(datePlusDays(firstSessionDate, 2)));

    }

    @Test
    public void findPair() {
        Optional<Event> pair = systemUnderTest.findPair(user, sessionDate);

        UserInfo actual = pair.get().getPartner();

        Assert.assertEquals(firstPeer, actual);
    }


    private UserInfo newDummyUser(Date lastSessionDate) {
        UserInfo user = new UserInfo();
        int randomInt = ThreadLocalRandom.current().nextInt();
        user.setUserId(randomInt);
        user.setFirstName("" + randomInt);
        user.setLastPairDate(lastSessionDate);
        return user;
    }

    public Date datePlusDays(Date date, int days) {
        return Date.from(date.toInstant().plus(days, ChronoUnit.DAYS));
    }

}