package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrivateEventParams {
    private long userId;
    private int from;
    private int size;
}
