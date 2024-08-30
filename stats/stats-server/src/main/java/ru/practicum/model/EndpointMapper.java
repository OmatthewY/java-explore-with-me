package ru.practicum.model;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.stat.EndpointHitDTO;
import ru.practicum.stat.ViewStatsDTO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface EndpointMapper {

    @Mapping(target = "id", ignore = true)
    EndpointHit toEndpointHit(EndpointHitDTO dto);

    EndpointHitDTO toEndpointHitDTO(EndpointHit endpointHit);

    @Mapping(target = "hits", ignore = true)
    ViewStatsDTO toViewStatsDTO(EndpointHit model);
}
