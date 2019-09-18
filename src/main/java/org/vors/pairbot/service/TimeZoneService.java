package org.vors.pairbot.service;

import net.iakovlev.timeshape.TimeZoneEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vors.pairbot.model.UserInfo;
import org.vors.pairbot.repository.UserRepository;

import java.time.ZoneId;
import java.util.Optional;

@Component
public class TimeZoneService {
    private TimeZoneEngine tzEngine = TimeZoneEngine.initialize();
    @Autowired
    private UserRepository userRepository;

    public Optional<ZoneId> setTimeZone(float lat, float lng, UserInfo user) {
        Optional<ZoneId> maybeZone = getTimeZone(lat, lng);

        maybeZone.ifPresent(zone -> {
            user.setTimezone(zone);
            userRepository.save(user);
        });

        return maybeZone;
    }

    private Optional<ZoneId> getTimeZone(float lat, float lng) {

        return tzEngine.query(lat, lng);
    }

}
