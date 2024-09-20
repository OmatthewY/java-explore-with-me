package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;

import java.util.Collection;
import java.util.List;

public interface EventService {
    List<EventShortDto> getAll(PrivateEventParams params);

    EventFullDto create(long userId, NewEventDto newEventDto);

    EventFullDto getById(long eventId);

    EventFullDto update(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<EventShortDto> getAll(PublicEventRequestParams params);

    List<EventFullDto> getAll(AdminEventRequestParams params);

    EventFullDto getById(long userId, long eventId);

    EventFullDto update(long eventId, UpdateEventAdminRequest event);

    Collection<Event> getByIds(List<Long> events);
}
