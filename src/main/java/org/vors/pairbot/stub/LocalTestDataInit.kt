package org.vors.pairbot.stub

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.vors.pairbot.model.Team
import org.vors.pairbot.model.UserInfo
import org.vors.pairbot.repository.TeamRepository
import org.vors.pairbot.repository.UserRepository
import java.util.*
import javax.annotation.PostConstruct

@Component
@Profile("test_local")
class LocalTestDataInit(
        var teamRepository: TeamRepository,
        var userRepository: UserRepository) {
    @PostConstruct
    fun initData(){
        //todo: create team, share link with current user

        //todo: create users, add to currentUser team.
    }

    private fun createDummyUser(): UserInfo {
        val user = UserInfo(0, "DummyUser" + Date())

        userRepository.save(user)
        return user
    }

    private fun createDummyTeam(creator: UserInfo) {

        val team = Team(UUID.randomUUID(), creator)

        userRepository.save(creator)
        teamRepository.save(team)
    }
}