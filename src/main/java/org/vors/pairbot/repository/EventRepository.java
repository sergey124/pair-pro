package org.vors.pairbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vors.pairbot.model.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
}