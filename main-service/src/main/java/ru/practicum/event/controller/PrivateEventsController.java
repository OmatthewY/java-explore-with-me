package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class PrivateEventsController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getAll(@PathVariable("userId") long userId,
                                      @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from,
                                      @RequestParam(value = "size", defaultValue = "10") @Positive int size) {

        return eventService.getAll(new PrivateEventParams(userId, from, size));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable("userId") long userId,
                               @Valid @RequestBody NewEventDto event) {
        return eventService.create(userId, event);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getById(@PathVariable("userId") long userId,
                                @PathVariable("eventId") long eventId) {
        return eventService.getById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable("userId") long userId,
                               @PathVariable("eventId") long eventId,
                               @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        return eventService.update(userId, eventId, updateEventUserRequest);
    }
}
