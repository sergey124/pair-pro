package org.vors.pairbot.scheduled

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.vors.pairbot.service.BulkService
import org.vors.pairbot.service.NotificationService
import java.time.Duration

@Component
class ScheduledJobs(
        @Value("\${notification.upcoming.scan.period.seconds:30}")
        var scanPeriodSeconds: Int = -1,
        @Value("\${notification.upcoming.seconds:60}")
        var notifyUpcomingInSeconds: Int = -1,
        val notificationService: NotificationService,
        val bulkService: BulkService
) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Scheduled(fixedRateString = "#{\${notification.upcoming.scan.period.seconds} * 1000}") // every N sec
    fun notifyAllUpcoming() {
        notificationService.notifyAllUpcomingIn(
                Duration.ofSeconds(notifyUpcomingInSeconds.toLong()),
                Duration.ofSeconds(scanPeriodSeconds.toLong()))
    }

    @Scheduled(cron = "0 0 10 * * TUE") // every day at 10:00
    fun scheduleSessions() {
        logger.info("Scheduling sessions for all")
        bulkService.scheduleSessionForAll()
    }

}
