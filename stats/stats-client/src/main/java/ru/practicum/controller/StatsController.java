package ru.practicum.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stat.StatsParams;
import ru.practicum.client.StatClient;
import ru.practicum.stat.EndpointHitDTO;
import ru.practicum.stat.ViewStatsDTO;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
public class StatsController {

    private final StatClient statClient;

    @PostMapping("/hit")
    public ResponseEntity<Object> saveStats(@Valid @RequestBody EndpointHitDTO hitDto) {
        return statClient.saveStats(hitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDTO> getStats(@Valid @RequestParam("start") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                       @Valid @RequestParam("end") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                       @RequestParam("uris") List<String> uris,
                                       @RequestParam(value = "unique", defaultValue = "false") Boolean unique) {
        StatsParams statsParams = StatsParams.builder()
                .start(start)
                .end(end)
                .unique(unique)
                .uris(uris)
                .build();

        return statClient.getStats(statsParams);
    }
}
