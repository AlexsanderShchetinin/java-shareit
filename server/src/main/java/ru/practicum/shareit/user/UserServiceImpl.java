package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicatedException;
import ru.practicum.shareit.exception.MyNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserListMapperImpl;
import ru.practicum.shareit.user.mapper.UserMapperImpl;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapperImpl userMapper;
    private final UserListMapperImpl userListMapper;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UserDto create(UserDto userDto) {
        if (!userRepository.getByEmail(userDto.getEmail()).isEmpty()) {
            throw new DuplicatedException("Пользователь с таким email уже существует");
        }
        User user = userMapper.toModel(userDto);
        User userFromRep = userRepository.save(user);
        return userMapper.toDto(userFromRep);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UserDto update(long userId, UserDto userDto) {
        User returnedUser = userRepository.findById(userId)
                .orElseThrow(() -> new MyNotFoundException("Пользователя с id=" + userDto.getId() + " не существует."));

        User user = userMapper.toModel(userDto);
        user.setId(userId);
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            user.setEmail(returnedUser.getEmail());
        } else if (!userRepository.getByEmail(user.getEmail()).isEmpty() &&
                !user.getEmail().equals(returnedUser.getEmail())) {
            throw new DuplicatedException("Пользователь с таким email уже существует");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(returnedUser.getName());
        }

        User newUser = userRepository.save(user);
        return userMapper.toDto(newUser);
    }

    @Override
    public UserDto getById(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new MyNotFoundException("Пользователя с id=" + id + " не существует."));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userListMapper.toListDto(userRepository.findAll());

    }

    @Override
    public void delete(long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new MyNotFoundException("Пользователя с id=" + id + " не существует."));
        userRepository.deleteById(id);
    }
}
