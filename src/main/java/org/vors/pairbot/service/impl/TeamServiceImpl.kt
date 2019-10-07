package org.vors.pairbot.service.impl

import org.springframework.stereotype.Component
import org.vors.pairbot.model.Team
import org.vors.pairbot.model.UserInfo
import org.vors.pairbot.repository.TeamRepository
import org.vors.pairbot.repository.UserRepository
import org.vors.pairbot.service.TeamService
import java.util.*

@Component
class TeamServiceImpl(
        private val teamRepository: TeamRepository,
        private val userRepository: UserRepository
) : TeamService {

    override fun newTeam(creator: UserInfo): Team {
        val team = Team(
                UUID.randomUUID(),
                creator
        )
        team.addMember(creator)

        //todo: change to one save with cascade = merge and optional save if new UserInfo ?
        teamRepository.save(team)
        userRepository.save(creator)

        return team
    }
}