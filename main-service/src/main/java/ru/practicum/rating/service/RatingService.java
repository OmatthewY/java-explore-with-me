package ru.practicum.rating.service;

import ru.practicum.rating.dto.RatingDto;

import java.util.List;

public interface RatingService {
    List<RatingDto> getAllById(long userId, int from, int size);

    void addRating(long userId, long eventId, boolean isLike);

    void removeRating(long userId, long eventId, boolean isLike);
}
