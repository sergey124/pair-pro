package org.vors.pairbot.service

import org.springframework.stereotype.Component
import org.vors.pairbot.model.UserInfo

@Component
class ChatService {
    fun getPrivateChatId(user: UserInfo): Long {
        return user.userId.toLong()
    }
}
