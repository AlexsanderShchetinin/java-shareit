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
        return ResponseEntity.ok().body(userService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable long id) {
        return ResponseEntity.ok().body(userService.getById(id));
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody UserDto user) {
        return ResponseEntity.ok().body(userService.create(user));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> update(@PathVariable long userId,
                                          @RequestBody UserDto newUser) {
        return ResponseEntity.ok().body(userService.update(userId, newUser));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        userService.delete(id);
    }


}
