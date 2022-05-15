package org.vors.pairbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.vors.pairbot.model.Team
import org.vors.pairbot.model.UserInfo

import java.util.Date
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<UserInfo, Long> {
    fun findByUserId(userId: Long?): Optional<UserInfo>

    fun existsByUserId(userId: Long?): Boolean

    @Query(value = """
        SELECT u FROM UserInfo AS u 
        WHERE u.team = :team 
            AND u != :user 
            AND NOT EXISTS (
                SELECT e FROM Event AS e 
                JOIN Participant AS p ON e = p.event 
                WHERE p.user = u 
                    AND e.date > :date
            ) 
    """)
    fun findPartnersByNoEventsAfter(date: Date, user: UserInfo, team: Team): List<UserInfo>

    @Query(value = """
        SELECT u FROM UserInfo AS u 
        WHERE NOT EXISTS (
            SELECT e FROM Event AS e
            JOIN Participant AS p ON e = p.event
            WHERE p.user = u
            AND e.date > :date
        )
    """)
    fun findByNoEventsAfter(date: Date): List<UserInfo>

}