package ru.practicum.user.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exeption.NotFoundException;
import ru.practicum.user.dto.AdminUserParams;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.QUser;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAll(AdminUserParams params) {
        QUser user = QUser.user;

        if (params.getIds() != null && !params.getIds().isEmpty()) {
            BooleanExpression condition = user.id.in(params.getIds());
            List<User> users = (List<User>) userRepository.findAll(condition);
            return users.stream()
                    .map(userMapper::toUserDto)
                    .collect(Collectors.toList());
        }

        PageRequest pageRequest = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        Page<User> usersPage = userRepository.findAll(pageRequest);

        return usersPage.stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto create(NewUserRequest dto) {
        User user = userMapper.toUser(dto);

        return userMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID = " + id + " не найден"));

        userRepository.deleteById(id);
    }
}
