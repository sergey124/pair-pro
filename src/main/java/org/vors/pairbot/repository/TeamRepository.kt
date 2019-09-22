package org.vors.pairbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.vors.pairbot.model.Team

import java.util.Optional
import java.util.UUID

@Repository
interface TeamRepository : JpaRepository<Team, Long> {

    fun findByToken(token: UUID): Optional<Team>
}