package ru.practicum.rating.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.service.RatingService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/ratings")
public class PrivateRatingController {
    private final RatingService ratingService;

    @GetMapping
    public List<RatingDto> getAllById(@PathVariable("userId") long userId,
                                      @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from,
                                      @RequestParam(value = "size", defaultValue = "10") @Positive int size) {
        return ratingService.getAllById(userId, from, size);
    }

    @PatchMapping("/add")
    public void add(@Min(0) @PathVariable("userId") long userId,
                    @Min(0) @RequestParam("eventId") long eventId,
                    @RequestParam ("isLike") boolean isLike) {
        int likeValue = isLike ? 1 : -1;
        ratingService.addRating(userId, eventId, likeValue);
    }

    @DeleteMapping("/remove")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@Min(0) @PathVariable("userId") long userId,
                       @Min(0) @RequestParam("eventId") long eventId,
                       @RequestParam ("isLike") boolean isLike) {
        int likeValue = isLike ? 1 : -1;
        ratingService.removeRating(userId, eventId, likeValue);
    }
}
