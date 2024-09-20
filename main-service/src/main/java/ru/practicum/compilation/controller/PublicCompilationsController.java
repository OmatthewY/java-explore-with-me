package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.PublicCompilationParams;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class PublicCompilationsController {
    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getAll(@RequestParam(value = "pinned", required = false) Boolean pinned,
                                       @RequestParam(value = "from", defaultValue = "0") int from,
                                       @RequestParam(value = "size", defaultValue = "10") int size) {
        PublicCompilationParams params = PublicCompilationParams.builder()
                .pinned(pinned)
                .from(from)
                .size(size)
                .build();
        return compilationService.getAll(params);
    }

    @GetMapping("/{compId}")
    public CompilationDto getById(@PathVariable("compId") long compId) {
        return compilationService.getById(compId);
    }
}
