package org.vors.pairbot.service

import net.iakovlev.timeshape.TimeZoneEngine
import org.springframework.stereotype.Component
import org.vors.pairbot.model.UserInfo
import org.vors.pairbot.repository.UserRepository
import java.time.ZoneId
import java.util.*

@Component
class TimeZoneService (
        val userRepository: UserRepository
) {
    private val tzEngine = TimeZoneEngine.initialize()

    fun setTimeZone(lat: Float, lng: Float, user: UserInfo): Optional<ZoneId> {
        val maybeZone = getTimeZone(lat, lng)

        maybeZone.ifPresent { zone ->
            user.timezone = zone
            userRepository.save(user)
        }

        return maybeZone
    }

    private fun getTimeZone(lat: Float, lng: Float): Optional<ZoneId> {

        return tzEngine.query(lat.toDouble(), lng.toDouble())
    }

}
