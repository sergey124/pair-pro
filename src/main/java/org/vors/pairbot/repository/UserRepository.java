package org.vors.pairbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.vors.pairbot.model.Team;
import org.vors.pairbot.model.UserInfo;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByUserId(Integer userId);

    boolean existsByUserId(Integer userId);

    @Query(value =
            "SELECT u FROM UserInfo AS u " +
                    "WHERE u.team = :team " +
                    "AND u != :user " +
                    "AND NOT EXISTS (" +
                        "SELECT e FROM Event AS e " +
                        "JOIN Participant AS p ON e = p.event " +
                        "WHERE p.user = u " +
                        "AND e.date > :date" +
                    ")")
    List<UserInfo> findByNoEventsAfter(Date date, UserInfo user, Team team);

}