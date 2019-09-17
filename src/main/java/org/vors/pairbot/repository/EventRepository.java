package org.vors.pairbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.vors.pairbot.model.Event;
import org.vors.pairbot.model.UserInfo;

import java.util.Date;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query(value = "SELECT e FROM Event e " +
            "JOIN Participant p ON e = p.event " +
            "WHERE e.date > :date " +
            "AND p.user = :user " +
            "ORDER BY e.date")
    List<Event> getEventsAfter(Date date, UserInfo user);

    boolean existsByDateAfterAndParticipants_User(Date date, UserInfo user);

    List<Event> findByDateBetweenAndAcceptedTrue(Date start, Date end);

}