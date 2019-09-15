package org.vors.pairbot.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.vors.pairbot.service.NotificationService;

import java.time.Duration;

@Component
public class ScheduledJobs {
    private static final Duration SCAN_PERIOD = Duration.ofMinutes(30);

    @Autowired
    private NotificationService notificationService;

    @Scheduled(cron = "0 0/30 * * * *") // every 30 min
    public void updateStatsForAll() {
        notificationService.notifyAllUpcomingIn(Duration.ofHours(1), SCAN_PERIOD);
    }
}
