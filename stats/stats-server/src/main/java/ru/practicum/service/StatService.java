package ru.practicum.service;

import ru.practicum.stat.EndpointHitDTO;
import ru.practicum.stat.StatsParams;
import ru.practicum.stat.ViewStatsDTO;

import java.util.List;

public interface StatService {
    EndpointHitDTO save(EndpointHitDTO dto);

    List<ViewStatsDTO> getStats(StatsParams params);
}
