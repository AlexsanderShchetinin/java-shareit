package ru.practicum.shareit.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> create(User user);

    Optional<User> update(User newUser);

    List<User> getAll();

    Optional<User> get(long id);

    List<User> getByEmail(String email);

    void delete(long id);
}
