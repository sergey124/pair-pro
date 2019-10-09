package org.vors.pairbot.scheduled

import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.vors.pairbot.service.NotificationService
import java.time.Duration

@Component
class ScheduledJobs(
        @Value("\${notification.upcoming.scan.period.seconds:30}")
        var scanPeriodSeconds: Int = -1,
        @Value("\${notification.upcoming.seconds:60}")
        var notifyUpcomingInSeconds: Int = -1,
        val notificationService: NotificationService
) {
    @Scheduled(fixedRateString = "#{\${notification.upcoming.scan.period.seconds} * 1000}") // every 30 min
    fun notifyAllUpcoming() {
        notificationService.notifyAllUpcomingIn(
                Duration.ofSeconds(notifyUpcomingInSeconds.toLong()),
                Duration.ofSeconds(scanPeriodSeconds.toLong()))
    }

}
