package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Collection<UserDto>> getAll() {
        log.info("{}[34m==> GET /all users <==", (char) 27);
        return ResponseEntity.ok().body(userService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable long id) {
        log.info("{}[34m==> GET /user by id={} <==", (char) 27, id);
        return ResponseEntity.ok().body(userService.getById(id));
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody @Validated(Marker.Create.class) UserDto user) {
        log.info("{}[34mCreate User: {} - STARTED", (char) 27, user.getName());
        UserDto createdUser = userService.create(user);
        log.info("{}[34mUser {} with id={} - CREATED", (char) 27, createdUser.getName(), createdUser.getId());
        return ResponseEntity.ok().body(createdUser);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> update(@PathVariable long userId,
                                          @RequestBody @Validated(Marker.Update.class) UserDto newUser) {
        log.info("{}[34m==> PATCH / Update User: {} - STARTED <==", (char) 27, newUser.getName());
        UserDto updatedUser = userService.update(userId, newUser);
        log.info("{}[34m==> User {}  with id={} UPDATED <==", (char) 27, newUser.getName(), newUser.getId());
        return ResponseEntity.ok().body(updatedUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("{}[34m==> DELETE / User with id={} <==", (char) 27, id);
        userService.delete(id);
    }


}
