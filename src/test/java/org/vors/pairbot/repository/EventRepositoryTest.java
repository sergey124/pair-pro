package org.vors.pairbot.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.vors.pairbot.PairbotApplication;
import org.vors.pairbot.model.Event;
import org.vors.pairbot.model.Participant;
import org.vors.pairbot.model.UserInfo;
import org.vors.pairbot.service.TimeService;

import java.util.Date;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PairbotApplication.class)
public class EventRepositoryTest {

    @Autowired
    private EventRepository systemUnderTest;

    private TimeService timeService = new TimeService();
    private Date date = new Date();
    private Event event;
    private UserInfo user;

    @Before
    public void setup() {
        user = new UserInfo();

        event = new Event();
        event.addParticipant(new Participant(user));
    }

    @Test
    public void givenEventBeforeGivenDate_whenExistsByDate_thenFalse() {
        //given
        saveEventWithDateShift(-1);

        //then
        assertFalse(systemUnderTest.existsByDateAfterAndParticipants_User(date, user));
    }

    @Test
    public void givenEventAfterGivenDate_whenExistsByDate_thenTrue() {
        //given
        saveEventWithDateShift(1);

        //then
        assertTrue(systemUnderTest.existsByDateAfterAndParticipants_User(date, user));
    }

    private void saveEventWithDateShift(int hours) {
        event.setDate(timeService.datePlusHours(date, hours));
        systemUnderTest.save(event);
    }

}