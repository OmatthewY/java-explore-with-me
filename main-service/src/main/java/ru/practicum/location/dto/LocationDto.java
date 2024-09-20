package ru.practicum.location.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationDto {
    @Min(value = -90, message = "Широта должна быть не менее -90")
    @Max(value = 90, message = "Широта должна быть не более 90")
    private float lat;

    @Min(value = -180, message = "Долгота должна быть не менее -180")
    @Max(value = 180, message = "Долгота должна быть не более 180")
    private float lon;
}
