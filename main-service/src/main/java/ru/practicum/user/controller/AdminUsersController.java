package ru.practicum.user.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.AdminUserParams;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import java.util.List;
import java.util.Set;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminUsersController {
    private final UserService userService;

    @GetMapping("/users")
    public List<UserDto> getAll(@RequestParam(value = "ids", required = false) Set<Long> ids,
                                @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from,
                                @RequestParam(value = "size", defaultValue = "10") @Positive int size) {
        AdminUserParams params = AdminUserParams.builder()
                .ids(ids)
                .from(from)
                .size(size)
                .build();
        return userService.getAll(params);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Validated @RequestBody NewUserRequest dto) {
        return userService.create(dto);
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long userId) {
        userService.delete(userId);
    }
}
