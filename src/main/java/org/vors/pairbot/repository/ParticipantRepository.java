package org.vors.pairbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vors.pairbot.model.Participant;
import org.vors.pairbot.model.ParticipantId;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, ParticipantId> {
}