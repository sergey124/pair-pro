package org.vors.pairbot.stub

import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.vors.pairbot.service.BulkService

@Component
@Profile("test_local")
open class ScheduledMockActions(
        val bulkService: BulkService) {

    @Scheduled(cron = "*/30 * * * * *") // every N sec
    fun scheduleSessions() {
        bulkService.scheduleSessionForAll()
    }
}
