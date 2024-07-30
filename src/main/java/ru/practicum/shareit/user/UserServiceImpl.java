package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicatedException;
import ru.practicum.shareit.exception.MyNotFoundException;
import ru.practicum.shareit.exception.RepositoryReceiveException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserListMapperImpl;
import ru.practicum.shareit.user.mapper.UserMapperImpl;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapperImpl userMapper;
    private final UserListMapperImpl userListMapper;

    @Override
    public UserDto create(UserDto userDto) {
        if (!userRepository.getByEmail(userDto.getEmail()).isEmpty()) {
            throw new DuplicatedException("Пользоватедль с таким email уже существует");
        }
        User user = userMapper.toModel(userDto);
        User userFromRep = userRepository.create(user)
                .orElseThrow(() -> new RepositoryReceiveException("Ошибка создания User:" + user));
        return userMapper.toDto(userFromRep);
    }

    @Override
    public UserDto update(long userId, UserDto userDto) {
        User returnedUser = userRepository.get(userId)
                .orElseThrow(() -> new MyNotFoundException("Пользователя с id=" + userDto.getId() + " не существует."));

        User user = userMapper.toModel(userDto);
        user.setId(userId);
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            user.setEmail(returnedUser.getEmail());
        } else if (!userRepository.getByEmail(user.getEmail()).isEmpty() &&
                !user.getEmail().equals(returnedUser.getEmail())) {
            throw new DuplicatedException("Пользоватедль с таким email уже существует");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(returnedUser.getName());
        }

        User newUser = userRepository.update(user)
                .orElseThrow(() -> new RepositoryReceiveException("Ошибка при обновлении User:" + user));
        return userMapper.toDto(newUser);
    }

    @Override
    public UserDto getById(long id) {
        User user = userRepository.get(id)
                .orElseThrow(() -> new MyNotFoundException("Пользователя с id=" + id + " не существует."));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userListMapper.toListDto(userRepository.getAll());

    }

    @Override
    public void delete(long id) {
        userRepository.get(id)
                .orElseThrow(() -> new MyNotFoundException("Пользователя с id=" + id + " не существует."));
        userRepository.delete(id);
    }
}
