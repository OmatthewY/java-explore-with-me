package ru.practicum.compilation.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatClient;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.PublicCompilationParams;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.model.QCompilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exeption.NotFoundException;
import ru.practicum.request.dto.EventCountByRequest;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.stat.StatsParams;
import ru.practicum.stat.ViewStatsDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final StatClient statClient;
    private final EventMapper eventMapper;


    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        Collection<Event> events = new ArrayList<>();
        if (newCompilationDto.getEvents() != null) {
            events = eventRepository.findByIdIn(newCompilationDto.getEvents());
        }
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto, events);
        Compilation saved = compilationRepository.save(compilation);
        List<EventShortDto> list = getEventShortDtos(saved);
        return compilationMapper.toCompilationDto(saved, list);
    }

    private List<EventShortDto> getEventShortDtos(Compilation saved) {
        List<Event> compEvents = (List<Event>) saved.getEvents();

        List<EventCountByRequest> eventsIdWithViews = requestRepository.findConfirmedRequestWithoutLimitCheck(compEvents);


        List<String> uris = eventsIdWithViews.stream().map(ev -> "/events/" + ev.getEventId()).toList();

        StatsParams statsParams = StatsParams.builder().uris(uris).unique(true).start(LocalDateTime.now().minusYears(100)).end(LocalDateTime.now()).build();

        List<ViewStatsDTO> viewStatsDTOS = statClient.getStats(statsParams);

        return eventsIdWithViews.stream().map(ev -> {
            Event finalEvent = compEvents.stream().filter(e -> e.getId().equals(ev.getEventId())).findFirst().orElseThrow(() -> new IllegalStateException("Событие не найдено: " + ev.getEventId()));

            long views = viewStatsDTOS.stream().filter(stat -> stat.getUri().equals("/events/" + ev.getEventId())).map(ViewStatsDTO::getHits).findFirst().orElse(0L);
            finalEvent.setConfirmedRequests(Math.toIntExact(ev.getCount()));
            return eventMapper.toEventShortDto(finalEvent, views);
        }).toList();
    }

    @Override
    @Transactional
    public void delete(long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> new NotFoundException("Подборка с ID = " + compId + " не найдена"));

        compilationRepository.delete(compilation);
        compilationRepository.flush();
    }

    @Override
    @Transactional
    public CompilationDto update(long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> new NotFoundException("Подборка с ID = " + compId + " не найдена"));
        if (updateCompilationRequest.getEvents() != null) {
            compilation.setEvents(eventRepository.findByIdIn(updateCompilationRequest.getEvents()));
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }

        Compilation saved = compilationRepository.save(compilation);
        List<EventShortDto> list = getEventShortDtos(saved);

        return compilationMapper.toCompilationDto(saved, list);
    }

    @Override
    public List<CompilationDto> getAll(PublicCompilationParams params) {
        QCompilation compilation = QCompilation.compilation;
        List<BooleanExpression> conditions = new ArrayList<>();

        if (params.getPinned() != null) {
            conditions.add(compilation.pinned.eq(params.getPinned()));
        }

        BooleanExpression finalCondition = conditions.stream().reduce(BooleanExpression::and).orElse(compilation.isNotNull());

        PageRequest pageRequest = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());

        assert finalCondition != null;
        Iterable<Compilation> compilationsIterable = compilationRepository.findAll(finalCondition, pageRequest);

        List<Compilation> compilations = StreamSupport.stream(compilationsIterable.spliterator(), false).toList();

        return compilations.stream().map(comp -> compilationMapper.toCompilationDto(comp, getEventShortDtos(comp))).toList();
    }

    @Override
    public CompilationDto getById(long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> new NotFoundException("Подборка с ID = " + compId + " не найдена"));

        return compilationMapper.toCompilationDto(compilation, getEventShortDtos(compilation));
    }
}
