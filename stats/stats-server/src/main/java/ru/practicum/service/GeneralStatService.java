package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.EndpointMapper;
import ru.practicum.repository.StatRepository;
import ru.practicum.stat.EndpointHitDTO;
import ru.practicum.stat.StatsParams;
import ru.practicum.stat.ViewStatsDTO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GeneralStatService implements StatService {

    private final StatRepository statRepository;
    private final EndpointMapper endpointMapper;

    @Override
    @Transactional
    public EndpointHitDTO save(EndpointHitDTO dto) {
        EndpointHit endpointHit = statRepository.save(endpointMapper.toEndpointHit(dto));

        return endpointMapper.toEndpointHitDTO(endpointHit);
    }

    @Override
    public List<ViewStatsDTO> getStats(StatsParams params) {
        boolean unique = params.getUnique();
        boolean urisPresent = !params.getUris().isEmpty();

        if (unique && urisPresent) {
            return statRepository.findByUniqueIp(params.getStart(), params.getEnd(), params.getUris());
        }

        if (unique) {
            return statRepository.findAllByUniqueIp(params.getStart(), params.getEnd());
        }

        if (urisPresent) {
            return statRepository.findByNonUniqueIp(params.getStart(), params.getEnd(), params.getUris());
        }

        return statRepository.findAllByNonUniqueIp(params.getStart(), params.getEnd());
    }
}
