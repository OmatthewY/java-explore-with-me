package ru.practicum.rating.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.dto.EventRatingDto;
import ru.practicum.event.model.Event;
import ru.practicum.rating.model.Rating;
import ru.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long>, QuerydslPredicateExecutor<Rating> {
    Optional<Rating> findByUserAndEvent(User user, Event event);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.event = :event AND r.isLike = 1")
    int countLikesByEvent(@Param("event") Event event);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.event = :event AND r.isLike < 0")
    int countDislikesByEvent(@Param("event") Event event);

    @Query(value = "SELECT new ru.practicum.event.dto.EventRatingDto(e.id, " +
            "SUM(CASE WHEN r.isLike = 1 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.isLike < 0 THEN 1 ELSE 0 END)) " +
            "FROM Rating r " +
            "JOIN r.event e " +
            "WHERE e IN :events " +
            "GROUP BY e.id")
    List<EventRatingDto> countEventsRating(List<Event> events);
}
