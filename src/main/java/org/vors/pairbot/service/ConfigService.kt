package org.vors.pairbot.service

import org.apache.commons.lang3.math.NumberUtils
import org.springframework.stereotype.Component
import org.vors.pairbot.constant.Config


@Component
class ConfigService {

    fun getIntConfig(config: Config, defaultValue: Int): Int {
        val value = System.getenv(config.toString())

        return NumberUtils.toInt(value, defaultValue)
    }
}
