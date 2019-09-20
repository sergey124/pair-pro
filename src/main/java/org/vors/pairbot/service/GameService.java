package org.vors.pairbot.service;

import org.springframework.stereotype.Component;
import org.vors.pairbot.model.UserInfo;

@Component
public class GameService {

    public String xpShort(UserInfo user){
        return String.format("%dxp", user.getXp());
    }
}
