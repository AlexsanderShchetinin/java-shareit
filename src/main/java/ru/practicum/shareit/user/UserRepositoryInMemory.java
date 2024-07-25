package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryInMemory implements UserRepository {

    private final HashMap<Long, User> users;
    private long id = 0L;

    @Override
    public Optional<User> create(User user) {
        id += 1L;
        user.setId(id);
        users.put(id, user);
        return Optional.of(user);
    }

    @Override
    public Optional<User> update(User user) {
        users.put(user.getId(), user);
        return Optional.of(user);
    }

    @Override
    public List<User> getAll() {
        return users.values().stream().toList();
    }

    @Override
    public Optional<User> get(long id) {
        if (users.containsKey(id)) {
            return Optional.ofNullable(users.get(id));
        }
        log.warn("Пользователя с id={} не существует в памяти приложения для получения.", id);
        return Optional.empty();
    }

    @Override
    public List<User> getByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .toList();
    }

    @Override
    public void delete(long id) {
        users.remove(id);
    }
}
