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
        log.info("{}[32m ==> GET /all users <=={}[37m", (char) 27, (char) 27);
        return ResponseEntity.ok().body(userService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable long id) {
        log.info("{}[32m ==> GET /user by id={} <=={}[37m", (char) 27, id, (char) 27);
        return ResponseEntity.ok().body(userService.getById(id));
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody @Validated(Marker.Create.class) UserDto user) {
        log.info("{}[32m Create User: {} - STARTED{}[37m", (char) 27, user.getName(), (char) 27);
        UserDto createdUser = userService.create(user);
        log.info("{}[32m User {} with id={} - CREATED{}[37m",
                (char) 27, createdUser.getName(), createdUser.getId(), (char) 27);
        return ResponseEntity.ok().body(createdUser);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> update(@PathVariable long userId,
                                          @RequestBody @Validated(Marker.Update.class) UserDto newUser) {
        log.info("{}[32m ==> PATCH / Update User: {} - STARTED <=={}[37m", (char) 27, newUser.getName(), (char) 27);
        UserDto updatedUser = userService.update(userId, newUser);
        log.info("{}[32m ==> User {}  with id={} UPDATED <=={}[37m",
                (char) 27,newUser.getName(), newUser.getId(), (char) 27);
        return ResponseEntity.ok().body(updatedUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("{}[32m ==> DELETE / User with id={} <=={}[37m",(char) 27, id, (char) 27);
        userService.delete(id);
    }


}
