package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.event.model.Event;
import ru.practicum.request.dto.EventCountByRequest;
import ru.practicum.request.model.Request;
import ru.practicum.user.model.User;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long>, QuerydslPredicateExecutor<Event> {

    List<Request> findByEventAndRequester(Event event, User user);

    List<Request> findByRequester(User user);

    @Query("SELECT r FROM Request r WHERE r.id IN ?1")
    List<Request> findAllById(List<Long> requestIds);

    @Query(value = "SELECT new ru.practicum.request.dto.EventCountByRequest(e.id, COUNT(r.id)) " +
            "FROM Event e " +
            "LEFT JOIN Request r ON r.event.id = e.id AND r.status = 'CONFIRMED' " +
            "WHERE e.id IN ?1 " +
            "GROUP BY e.id, e.participantLimit " +
            "HAVING COUNT(r.id) >= e.participantLimit")
    List<EventCountByRequest> findConfirmedRequestWithLimitCheck(List<Event> events);

    @Query(value = "SELECT new ru.practicum.request.dto.EventCountByRequest(e.id, COUNT(r.id)) " +
            "FROM Event e " +
            "LEFT JOIN Request r ON r.event.id = e.id AND r.status = 'CONFIRMED' " +
            "WHERE e IN ?1 " +
            "GROUP BY e.id")
    List<EventCountByRequest> findConfirmedRequestWithoutLimitCheck(List<Event> events);

    @Query(value = "SELECT (COUNT(r.id)>=?2) " +
            "FROM Request r " +
            "WHERE r.event.id = ?1 AND r.status= 'CONFIRMED'")
    boolean isParticipantLimitReached(long eventId, int limit);

    @Query(value = "SELECT COUNT(r.id) AS count " +
            "FROM Request r " +
            "WHERE r.event.id = ?1 AND r.status = 'CONFIRMED'")
    Integer countConfirmedRequest(long eventId);

    List<Request> findByEvent(Event event);
}
