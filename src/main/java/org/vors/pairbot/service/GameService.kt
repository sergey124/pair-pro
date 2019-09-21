package org.vors.pairbot.service

import org.springframework.stereotype.Component
import org.vors.pairbot.model.UserInfo

@Component
class GameService {

    fun xpShort(user: UserInfo): String{
        return String.format("%dxp", user.xp)
    }
}
