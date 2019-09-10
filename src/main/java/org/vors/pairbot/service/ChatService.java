package org.vors.pairbot.service;

import org.springframework.stereotype.Component;
import org.vors.pairbot.model.UserInfo;

@Component
public class ChatService {
    public Long getPrivateChatId(UserInfo user) {
        return user.getUserId().longValue();
    }
}
