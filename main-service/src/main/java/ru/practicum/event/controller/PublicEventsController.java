package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatClient;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.PublicEventRequestParams;
import ru.practicum.event.model.Sort;
import ru.practicum.event.service.EventService;
import ru.practicum.exeption.WrongDateException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventsController {
    private final EventService eventService;

    private final StatClient statClient;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsPublic(@RequestParam(value = "text", required = false) String text,
                                               @RequestParam(value = "categories", required = false) List<Long> categories,
                                               @RequestParam(value = "paid", required = false) Boolean paid,
                                               @RequestParam(value = "rangeStart", required = false)
                                               @DateTimeFormat(pattern = ("yyyy-MM-dd HH:mm:ss")) LocalDateTime rangeStart,
                                               @RequestParam(value = "rangeEnd", required = false)
                                               @DateTimeFormat(pattern = ("yyyy-MM-dd HH:mm:ss")) LocalDateTime rangeEnd,
                                               @RequestParam(value = "onlyAvailable", defaultValue = "false") Boolean onlyAvailable,
                                               @RequestParam(value = "sort", required = false) Sort sort,
                                               @RequestParam(value = "from", defaultValue = "0") int from,
                                               @RequestParam(value = "size", defaultValue = "10") int size,
                                               HttpServletRequest request) {

        Map<String, LocalDateTime> ranges = validDate(rangeStart, rangeEnd);
        PublicEventRequestParams params = PublicEventRequestParams.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(ranges.get("rangeStart"))
                .rangeEnd(ranges.get("rangeEnd"))
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .build();
        List<EventShortDto> all = eventService.getAll(params);
        sendStats(request);
        return all;
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable("eventId") long eventId, HttpServletRequest request) {
        EventFullDto event = eventService.getById(eventId);
        sendStats(request);
        return event;
    }

    private Map<String, LocalDateTime> validDate(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeEnd != null && rangeStart != null && rangeEnd.isBefore(rangeStart)) {
            throw new WrongDateException("Дата окончания диапазона дложна быть после даты начала");
        }
        LocalDateTime effectiveRangeStart = rangeStart != null ? rangeStart : LocalDateTime.now();
        LocalDateTime effectiveRangeEnd = rangeEnd != null ? rangeEnd : effectiveRangeStart.plusYears(200);

        return Map.of("rangeStart", effectiveRangeStart, "rangeEnd", effectiveRangeEnd);
    }

    private void sendStats(HttpServletRequest request) {
        statClient.saveStats(request);
    }
}
