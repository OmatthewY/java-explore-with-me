package ru.practicum.rating.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.model.Rating;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface RatingMapper {

    @Mappings({
            @Mapping(source = "user.id", target = "userId"),
            @Mapping(target = "event.rating", ignore = true),
            @Mapping(target = "event.views", ignore = true)
    })
    RatingDto toDto(Rating rating);
}
