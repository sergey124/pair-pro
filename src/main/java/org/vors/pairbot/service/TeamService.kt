package org.vors.pairbot.service

import org.vors.pairbot.model.Team
import org.vors.pairbot.model.UserInfo

interface TeamService {

    fun newTeam(creator: UserInfo): Team

}