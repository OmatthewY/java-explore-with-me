package ru.practicum.user.service;

import ru.practicum.user.dto.AdminUserParams;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAll(AdminUserParams params);

    UserDto create(NewUserRequest dto);

    void delete(long id);
}
