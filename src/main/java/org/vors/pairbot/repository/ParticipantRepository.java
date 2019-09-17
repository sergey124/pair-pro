package org.vors.pairbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.vors.pairbot.model.Participant;
import org.vors.pairbot.model.ParticipantId;
import org.vors.pairbot.model.UserInfo;

import java.util.Date;
import java.util.List;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, ParticipantId> {
    @Query(value = "SELECT p FROM Participant p " +
            "JOIN Event e ON e = p.event " +
            "WHERE e.date > :date " +
            "AND (p.accepted = true OR p.accepted IS NULL)" +
            "AND p.user = :user " +
            "ORDER BY e.date")
    List<Participant> getParticipantsAfter(Date date, UserInfo user);
}