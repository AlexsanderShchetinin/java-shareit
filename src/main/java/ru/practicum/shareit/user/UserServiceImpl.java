package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicatedException;
import ru.practicum.shareit.exception.MyNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        if (!userRepository.getByEmail(userDto.getEmail()).isEmpty()) {
            throw new DuplicatedException("Пользоватедль с таким email уже существует");
        }
        User user = userRepository.create(UserMapper.toUser(userDto));
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto update(long userId, UserDto userDto) {
        User returnedUser = userRepository.get(userId)
                .orElseThrow(() -> new MyNotFoundException("Пользователя с id=" + userDto.getId() + " не существует."));

        User user = UserMapper.toUser(userDto);
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

        User newUser = userRepository.update(user);
        return UserMapper.toDto(newUser);
    }

    @Override
    public UserDto getById(long id) {
        User user = userRepository.get(id)
                .orElseThrow(() -> new MyNotFoundException("Пользователя с id=" + id + " не существует."));
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.getAll().stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public void delete(long id) {
        userRepository.get(id)
                .orElseThrow(() -> new MyNotFoundException("Пользователя с id=" + id + " не существует."));
        userRepository.delete(id);
    }
}
