package org.vors.pairbot.stub

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.vors.pairbot.model.Team
import org.vors.pairbot.model.UserInfo
import org.vors.pairbot.repository.TeamRepository
import org.vors.pairbot.repository.UserRepository
import org.vors.pairbot.service.TeamService
import org.vors.pairbot.service.impl.TeamServiceImpl

@Component
@Primary
@Profile("test_local")
open class TeamServiceDecorator(
        private val teamServiceImpl: TeamServiceImpl,
        private val teamRepository: TeamRepository,
        private val userRepository: UserRepository
) : TeamService {

    override fun newTeam(creator: UserInfo): Team {
        val team = teamServiceImpl.newTeam(creator)

        val dummy = newDummyUser()
        team.addMember(dummy)
        teamRepository.save(team)
        userRepository.save(dummy)

        return team
    }

    /**
     * For testing when there's no second account
     */
    private fun newDummyUser(): UserInfo {
        val user = UserInfo(0, "Partner")
        userRepository.save(user)
        return user
    }
}