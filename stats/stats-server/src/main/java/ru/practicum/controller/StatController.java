package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.service.StatService;
import ru.practicum.stat.EndpointHitDTO;
import ru.practicum.stat.StatsParams;
import ru.practicum.stat.ViewStatsDTO;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    @PostMapping("/hit")
    public EndpointHitDTO saveStats(@Valid @RequestBody EndpointHitDTO hitDto) {
        return statService.save(hitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDTO> getStats(
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(value = "uris", defaultValue = "") List<String> uris,
            @RequestParam(value = "unique", defaultValue = "false") Boolean unique) {

        StatsParams statsParams = StatsParams.builder()
                .start(start)
                .end(end)
                .unique(unique)
                .uris(uris)
                .build();

        return statService.getStats(statsParams);
    }
}
