package ru.practicum.event.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.location.model.Location;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface EventMapper {

    @Mapping(target = "views", source = "views")
    EventFullDto toEventFullDto(final Event event,final long views);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "state", source = "state")
    @Mapping(target = "createdOn", source = "createdOn")
    Event toEvent(final NewEventDto newEventDto, final Category category, final Location location,
                  final User initiator, final EventState state, LocalDateTime createdOn);

    @Mapping(target = "views", source = "views")
    EventShortDto toEventShortDto(final Event event,final long views);
}
