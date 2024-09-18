package ru.practicum.event.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.client.StatClient;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.QEvent;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exeption.ConflictException;
import ru.practicum.exeption.NotFoundException;
import ru.practicum.location.model.Location;
import ru.practicum.location.repository.LocationRepository;
import ru.practicum.request.dto.EventCountByRequest;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.stat.StatsParams;
import ru.practicum.stat.ViewStatsDTO;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final RequestRepository requestRepository;
    private final StatClient statClient;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    @Override
    public List<EventShortDto> getAll(PublicEventRequestParams params) {
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();

        int from = params.getFrom();
        int size = params.getSize();

        conditions.add(event.state.eq(EventState.PUBLISHED));

        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);

        conditions.add(event.eventDate.after(params.getRangeStart()));
        conditions.add(event.eventDate.before(params.getRangeEnd()));
        if (params.getText() != null) {
            conditions.add(event.description.containsIgnoreCase(params.getText()).or(event.annotation.containsIgnoreCase(params.getText())));
        }
        if (params.getCategories() != null && !params.getCategories().isEmpty()) {
            conditions.add(event.category.id.in(params.getCategories()));
        }
        if (params.getPaid() != null) {
            conditions.add(event.paid.eq(params.getPaid()));
        }
        BooleanExpression finalConditional = conditions.stream().reduce(BooleanExpression::and).get();

        List<Event> events = eventRepository.findAll(finalConditional, pageRequest).getContent();
        if (events.isEmpty()) {
            return Collections.emptyList();
        }
        List<EventCountByRequest> eventsIdWithConfirmedRequest;
        if (params.getOnlyAvailable()) {
            eventsIdWithConfirmedRequest = requestRepository.findConfirmedRequestWithLimitCheck(events);
        } else {
            eventsIdWithConfirmedRequest = requestRepository.findConfirmedRequestWithoutLimitCheck(events);
        }

        Map<Long, Long> eventIdToViewsMap = getViewStatsDTOS(eventsIdWithConfirmedRequest)
                .stream()
                .collect(Collectors.toMap(stat -> Long.parseLong(stat.getUri().split("/")[2]), ViewStatsDTO::getHits));

        return eventsIdWithConfirmedRequest.stream()
                .map(ev -> {
                    Event finalEvent = getFinalEvent(ev, events);
                    long views = eventIdToViewsMap.getOrDefault(finalEvent.getId(), 0L);
                    return eventMapper.toEventShortDto(finalEvent, views);
                })
                .collect(Collectors.toList());
    }

    private static long getViews(EventCountByRequest ev, List<ViewStatsDTO> viewStatsDTOS, Event finalEvent) {
        long views = viewStatsDTOS.stream()
                .filter(stat -> stat.getUri().equals("/events/" + ev.getEventId()))
                .map(ViewStatsDTO::getHits)
                .findFirst()
                .orElse(0L);
        finalEvent.setConfirmedRequests(Math.toIntExact(ev.getCount()));
        return views;
    }

    private static Event getFinalEvent(EventCountByRequest ev, List<Event> events) {
        return events.stream()
                .filter(e -> e.getId().equals(ev.getEventId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Event not found: " + ev.getEventId()));
    }

    @Override
    public EventFullDto getById(long eventId) {
        Event event = getEvent(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Event is not published");
        }

        Integer requests = requestRepository.countConfirmedRequest(eventId);

        long eventViews = getEventViews(event);

        event.setConfirmedRequests(requests);
        return eventMapper.toEventFullDto(event, eventViews);
    }

    private long getEventViews(Event event) {
        LocalDateTime start = event.getPublishedOn() != null ? event.getPublishedOn() : LocalDateTime.now().minusYears(200);
        List<String> listEndpoint = List.of("/events/" + event.getId());
        StatsParams statsParams = StatsParams.builder()
                .uris(listEndpoint)
                .unique(true)
                .start(start)
                .end(LocalDateTime.now())
                .build();
        List<ViewStatsDTO> stats = statClient.getStats(statsParams);
        if (stats.isEmpty()) {
            return 0;
        }
        return stats.getFirst().getHits();
    }

    @Override
    public List<EventFullDto> getAll(AdminEventRequestParams params) {
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();

        int from = params.getFrom();
        int size = params.getSize();

        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);

        conditions.add(event.eventDate.after(params.getRangeStart()));
        conditions.add(event.eventDate.before(params.getRangeEnd()));
        if (params.getUsers() != null && !params.getUsers().isEmpty()) {
            conditions.add(event.initiator.id.in(params.getUsers()));
        }
        if (params.getStates() != null && !params.getStates().isEmpty()) {
            conditions.add(event.state.in(params.getStates()));
        }
        if (params.getCategories() != null && !params.getCategories().isEmpty()) {
            conditions.add(event.category.id.in(params.getCategories()));
        }
        BooleanExpression finalConditional = conditions.stream().reduce(BooleanExpression::and).get();

        List<Event> events = eventRepository.findAll(finalConditional, pageRequest).getContent();
        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<EventCountByRequest> eventsIdWithConfirmedRequest
                = requestRepository.findConfirmedRequestWithoutLimitCheck(events);

        List<ViewStatsDTO> viewStatsDTOS = getViewStatsDTOS(eventsIdWithConfirmedRequest);

        Map<Long, Long> eventRequestsMap = eventsIdWithConfirmedRequest.stream()
                .collect(Collectors.toMap(EventCountByRequest::getEventId, EventCountByRequest::getCount));

        Map<Long, Long> eventViewsMap = viewStatsDTOS.stream()
                .collect(Collectors.toMap(
                        stat -> Long.parseLong(stat.getUri().split("/events/")[1]),
                        ViewStatsDTO::getHits
                ));

        return events.stream()
                .map(eventItem -> {
                    long requests = eventRequestsMap.getOrDefault(eventItem.getId(), 0L);
                    long views = eventViewsMap.getOrDefault(eventItem.getId(), 0L);

                    eventItem.setConfirmedRequests(Math.toIntExact(requests));

                    return eventMapper.toEventFullDto(eventItem, views);
                })
                .collect(Collectors.toList());
    }

    private List<ViewStatsDTO> getViewStatsDTOS(List<EventCountByRequest> eventsIdWithConfirmedRequest) {
        LocalDateTime start = eventsIdWithConfirmedRequest.stream()
                .map(ev -> getEvent(ev.getEventId()))
                .map(Event::getPublishedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusYears(100));

        List<String> uris = eventsIdWithConfirmedRequest.stream()
                .map(ev -> "/events/" + ev.getEventId())
                .collect(Collectors.toList());

        StatsParams statsParams = StatsParams.builder()
                .uris(uris)
                .unique(true)
                .start(start)
                .end(LocalDateTime.now())
                .build();

        return statClient.getStats(statsParams);
    }

    @Override
    public List<EventShortDto> getAll(PrivateEventParams params) {
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();
        conditions.add(event.initiator.id.eq(params.getUserId()));
        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .orElse(null);

        PageRequest pageRequest = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());

        List<Event> events = eventRepository.findAll(finalCondition, pageRequest).getContent();

        List<EventCountByRequest> eventsIdWithConfirmedRequest
                = requestRepository.findConfirmedRequestWithoutLimitCheck(events);

        Map<Long, Long> eventIdToViewsMap = getViewStatsDTOS(eventsIdWithConfirmedRequest)
                .stream()
                .collect(Collectors.toMap(stat -> Long.parseLong(stat.getUri().split("/")[2]), ViewStatsDTO::getHits));

        return eventsIdWithConfirmedRequest.stream()
                .map(ev -> {
                    Event finalEvent = getFinalEvent(ev, events);
                    long views = eventIdToViewsMap.getOrDefault(finalEvent.getId(), 0L);
                    return eventMapper.toEventShortDto(finalEvent, views);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto create(long userId, NewEventDto newEventDto) {
        User initiator = getUser(userId);
        if (newEventDto.getEventDate().minusHours(2).isBefore(LocalDateTime.now())) {
            throw new ConflictException("Different with now less than 2 hours");
        }
        Category category = getCategory(newEventDto.getCategory());

        Location location = eventMapper.toLocation(newEventDto.getLocation());
        location = locationRepository.save(location);

        Event event = eventMapper.toEvent(newEventDto, category, location, initiator, EventState.PENDING,
                LocalDateTime.now());
        event.setConfirmedRequests(0);

        Event saved = eventRepository.save(event);
        return eventMapper.toEventFullDto(saved, 0L);
    }

    @Override
    public EventFullDto getById(long userId, long eventId) {
        getUser(userId);
        Event event = getEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("The user is not the initiator of the event");
        }
        long eventViews = getEventViews(event);
        return eventMapper.toEventFullDto(event, eventViews);
    }

    @Override
    @Transactional
    public EventFullDto update(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = getEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("The user is not the initiator of the event");
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("You can't change an event that has already been published");
        }
        if (event.getEventDate().minusHours(2).isBefore(LocalDateTime.now())) {
            throw new ConflictException("Different with now less than 2 hours");
        }

        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategory() != null) {
            Category category = getCategory(updateEventUserRequest.getCategory());
            event.setCategory(category);
        }
        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getEventDate() != null) {
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getLocation() != null) {
            Location location = eventMapper.toLocation(updateEventUserRequest.getLocation());
            event.setLocation(locationRepository.save(location));
        }
        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }
        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction().equals(EventAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            } else if (updateEventUserRequest.getStateAction().equals(EventAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            }
        }
        Event saved = eventRepository.save(event);
        long eventViews = getEventViews(saved);
        return eventMapper.toEventFullDto(saved, eventViews);
    }

    @Override
    @Transactional
    public EventFullDto update(long eventId, UpdateEventAdminRequest eventDto) {
        Event savedEvent = getEvent(eventId);
        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(EventAction.PUBLISH_EVENT) && !savedEvent.getState().equals(EventState.PENDING)) {
                throw new ConflictException("Event in state " + savedEvent.getState() + " can not be published");
            }
            if (eventDto.getStateAction().equals(EventAction.REJECT_EVENT) && savedEvent.getState().equals(EventState.PUBLISHED)) {
                throw new ConflictException("Event in state " + savedEvent.getState() + " can not be rejected");
            }
            if (eventDto.getStateAction().equals(EventAction.REJECT_EVENT)) {
                savedEvent.setState(EventState.CANCELED);
            }
        }

        if (eventDto.getEventDate() != null) {
            if (savedEvent.getState().equals(EventState.PUBLISHED) && savedEvent.getPublishedOn().plusHours(1).isAfter(eventDto.getEventDate())) {
                throw new ConflictException("Different with publishedOn less than 1 hours");
            }
            savedEvent.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getAnnotation() != null && !eventDto.getAnnotation().isBlank()) {
            savedEvent.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getDescription() != null && !eventDto.getDescription().isBlank()) {
            savedEvent.setDescription(eventDto.getDescription());
        }
        if (eventDto.getLocation() != null) {
            Location location = eventMapper.toLocation(eventDto.getLocation());
            savedEvent.setLocation(locationRepository.save(location));
        }

        if (eventDto.getCategory() != null) {
            Category category = getCategory(eventDto.getCategory());
            savedEvent.setCategory(category);
        }
        if (eventDto.getPaid() != null) {
            savedEvent.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            savedEvent.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            savedEvent.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getTitle() != null && !eventDto.getTitle().isBlank()) {
            savedEvent.setTitle(eventDto.getTitle());
        }
        if (eventDto.getStateAction() != null && eventDto.getStateAction().equals(EventAction.PUBLISH_EVENT)) {
            savedEvent.setState(EventState.PUBLISHED);
        }
        savedEvent.setPublishedOn(LocalDateTime.now());
        Integer requests = requestRepository.countConfirmedRequest(eventId);
        long eventViews = getEventViews(savedEvent);

        Event updated = eventRepository.save(savedEvent);
        updated.setConfirmedRequests(requests);
        return eventMapper.toEventFullDto(updated, eventViews);
    }

    @Override
    public List<Event> getByIds(List<Long> events) {
        return eventRepository.findAllById(events);
    }

    private Event getEvent(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не найдено"));
    }

    private User getUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found with id" + userId));
    }

    private Category getCategory(long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id= " + categoryId + " was not found"));
    }
}
