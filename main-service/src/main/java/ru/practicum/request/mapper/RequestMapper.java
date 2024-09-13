package ru.practicum.request.mapper;

import org.mapstruct.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface RequestMapper {

    @Mappings({
            @Mapping(source = "event.id", target = "event"),
            @Mapping(source = "requester.id", target = "requester")
    })
    ParticipationRequestDto toParticipationRequestDto(Request request);
}
