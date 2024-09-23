package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRatingDto {
    private Long eventId;
    private long likes;
    private long dislikes;

    public Long getRating() {
        return likes - dislikes;
    }
}
