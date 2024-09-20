package ru.practicum.rating.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exeption.ConflictException;
import ru.practicum.exeption.NotFoundException;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.mapper.RatingMapper;
import ru.practicum.rating.model.QRating;
import ru.practicum.rating.model.Rating;
import ru.practicum.rating.repository.RatingRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RatingMapper ratingMapper;

    @Override
    public List<RatingDto> getAllById(long userId, int from, int size) {
        QRating rating = QRating.rating;
        List<BooleanExpression> conditions = new ArrayList<>();
        conditions.add(rating.user.id.eq(userId));
        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .orElse(null);

        PageRequest pageRequest = PageRequest.of(from / size, size);

        List<Rating> ratings = ratingRepository.findAll(finalCondition, pageRequest).getContent();

        return ratings.stream()
                .map(ratingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addRating(long userId, long eventId, boolean isLike) {
        User user = getUser(userId);
        Event event = getEvent(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("The initiator of the event can't add a request to participate in his event");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Cannot react because the event is not published");
        }

        Optional<Rating> existingRating = ratingRepository.findByUserAndEvent(user, event);

        if (existingRating.isPresent()) {
            Rating rating = existingRating.get();
            if (rating.getIsLike() == isLike) {
                throw new ConflictException((isLike ? "Like" : "Dislike") + " already exists");
            } else {
                rating.setIsLike(isLike);
                ratingMapper.toDto(ratingRepository.save(rating));
            }
        } else {
            // Если нет реакции, создаем новую
            Rating rating = Rating.builder()
                    .created(LocalDateTime.now())
                    .user(user)
                    .event(event)
                    .isLike(isLike)
                    .build();
            ratingMapper.toDto(ratingRepository.save(rating));
        }
    }

    @Transactional
    public void removeRating(long userId, long eventId, boolean isLike) {
        User user = getUser(userId);
        Event event = getEvent(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Cannot remove reaction because the event is not published");
        }

        Rating rating = getRating(user, event);

        if (rating.getIsLike() == isLike) {
            ratingRepository.delete(rating);
        } else {
            throw new ConflictException("No " + (isLike ? "like" : "dislike") + " found to remove");
        }
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id" + userId));
    }

    private Event getEvent(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));
    }

    private Rating getRating(User user, Event event) {
        return ratingRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new NotFoundException("Rating not found"));
    }
}
