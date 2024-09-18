package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestParamsUpdate;
import ru.practicum.request.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
@Validated
public class PrivateRequestController {
    private final RequestService requestService;

    @GetMapping("/requests")
    public List<ParticipationRequestDto> getAll(@PathVariable("userId") long userId) {
        return requestService.getAll(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable("userId") long userId,
                                          @RequestParam(value = "eventId") int eventId) {
        return requestService.create(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable("userId") long userId,
                                          @PathVariable("requestId") long requestId) {
        return requestService.cancel(userId, requestId);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsOnUserEvent(@PathVariable("userId") long userId,
                                                                @PathVariable("eventId") long eventId) {
        return requestService.findRequestsOnUserEvent(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatusRequestByUserAndEventId(@PathVariable("userId") long userId,
                                                                              @PathVariable("eventId") long eventId,
                                                                              @RequestBody EventRequestStatusUpdateRequest updateDto) {
        return requestService.updateStatus(new RequestParamsUpdate(userId, eventId, updateDto));
    }
}
