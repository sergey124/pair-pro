package org.vors.pairbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import org.vors.pairbot.model.Event;
import org.vors.pairbot.model.UserInfo;
import org.vors.pairbot.repository.EventRepository;
import org.vors.pairbot.repository.UserRepository;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Transactional
@Component
public class UserService {
    private Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private TimeService timeService;

    public UserInfo createAndSaveUser(User user) {
        return userRepository.save(createUserInfo(user));
    }

    private UserInfo createUserInfo(User user) {
        Integer userId = user.getId();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();

        return createUserInfo(userId, firstName, lastName);
    }

    private UserInfo createUserInfo(Integer userId, String firstName, String lastName) {
        return newUserInfo(userId, firstName, lastName);
    }

    private UserInfo newUserInfo(Integer id, String firstName, String lastName) {
        UserInfo newUser = new UserInfo();
        newUser.setUserId(id);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setCreatedDate(new Date());
        return newUser;
    }

    public UserInfo getExistingUser(Integer userId) {
        return findByUserId(userId).orElseThrow(() -> new IllegalStateException("Existing user not found for ID: " + userId));
    }

    public Optional<UserInfo> findByUserId(Integer userId) {
        return userRepository.findByUserId(userId);
    }

    public List<Event> findUpcomingEvents(Duration upcomingIn, Duration scanPeriod) {
        return eventRepository.findByDateBetweenAndAcceptedTrue(
                timeService.nowPlusDuration(upcomingIn.minus(scanPeriod)),
                timeService.nowPlusDuration(upcomingIn));
    }

}
