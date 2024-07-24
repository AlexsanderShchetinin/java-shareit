package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validator.Marker;

import java.util.Collection;

/**
 * Created Shchetinin Alexander
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;


    @GetMapping
    public Collection<UserDto> getAll() {
        log.info("==> GET /all users <==");
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable long id) {
        log.info("==> GET /user by id={} <==", id);
        return userService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Validated(Marker.Create.class) UserDto user) {
        log.info("Create User: {} - STARTED", user.getName());
        UserDto createdUser = userService.create(user);
        log.info("User {} with id={} - CREATED", createdUser.getName(), createdUser.getId());
        return createdUser;
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable long userId,
                          @RequestBody @Validated(Marker.Update.class) UserDto newUser) {
        log.info("==> PATCH / Update User: {} - STARTED <==", newUser.getName());
        UserDto updatedUser = userService.update(userId, newUser);
        log.info("==> User {}  with id={} UPDATED <==", newUser.getName(), newUser.getId());
        return updatedUser;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("==> DELETE / User with id={} <==", id);
        userService.delete(id);
    }


}
