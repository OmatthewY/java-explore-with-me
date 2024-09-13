package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.AdminEventRequestParams;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.exeption.WrongDateException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping(("/admin"))
public class AdminEventsController {
    private final EventService eventService;

    @GetMapping("/events")
    public List<EventFullDto> getEvents(@RequestParam(value = "users", required = false) List<Long> users,
                                        @RequestParam(value = "states", required = false) List<EventState> states,
                                        @RequestParam(value = "categories", required = false) List<Long> categories,
                                        @RequestParam(value = "rangeStart", required = false)
                                        @DateTimeFormat(pattern = ("yyyy-MM-dd HH:mm:ss")) LocalDateTime rangeStart,
                                        @RequestParam(value = "rangeEnd", required = false)
                                        @DateTimeFormat(pattern = ("yyyy-MM-dd HH:mm:ss")) LocalDateTime rangeEnd,
                                        @RequestParam(value = "from", defaultValue = "0") int from,
                                        @RequestParam(value = "size", defaultValue = "10") int size) {

        Map<String, LocalDateTime> ranges = validDate(rangeStart, rangeEnd);
        AdminEventRequestParams params = AdminEventRequestParams.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(ranges.get("rangeStart"))
                .rangeEnd(ranges.get("rangeEnd"))
                .from(from)
                .size(size)
                .build();
        return eventService.getAll(params);
    }

    @PatchMapping("/events/{eventId}")
    public EventFullDto updateEvent(@Positive @PathVariable long eventId,
                                    @Valid @RequestBody UpdateEventAdminRequest dto) {

        return eventService.update(eventId, dto);
    }

    private Map<String, LocalDateTime> validDate(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeEnd != null && rangeStart != null && rangeEnd.isBefore(rangeStart)) {
            throw new WrongDateException("Дата окончания диапазона дложна быть после даты начала");
        }
        LocalDateTime effectiveRangeStart = rangeStart != null ? rangeStart : LocalDateTime.now();
        LocalDateTime effectiveRangeEnd = rangeEnd != null ? rangeEnd : effectiveRangeStart.plusYears(200);

        return Map.of("rangeStart", effectiveRangeStart, "rangeEnd", effectiveRangeEnd);
    }
}
