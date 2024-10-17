package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.item.dto.CommentCreationDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validator.Marker;

/**
 * Created by Shchetinin Alexander 24.07.2024
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getAllByOwner(@RequestHeader("X-Sharer-User-Id") String ownerStr) {
        long ownerId;
        try {
            ownerId = Long.parseLong(ownerStr);
        } catch (NumberFormatException e) {
            log.warn("{}[31m ==> GET/items (METHOD getAllByOwner) <== catch except: {} {}[37m",
                    (char) 27, e.getMessage() + e.getCause(), (char) 27);
            throw new BadRequestException("ошибка преобразования Id владельца:" + e.getMessage());
        }

        log.info("{}[32m ==> GET /all items for owner with id={}<=={}[37m", (char) 27, ownerId, (char) 27);
        return itemClient.getAllByOwner(ownerId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") String ownerStr,
                                          @PathVariable long id) {
        long ownerId;
        try {
            ownerId = Long.parseLong(ownerStr);
        } catch (NumberFormatException e) {
            log.warn("{}[31m ==> GET/items/{id} (METHOD getById) <== catch except: {} {}[37m",
                    (char) 27, e.getMessage() + e.getCause(), (char) 27);
            throw new BadRequestException("ошибка преобразования Id владельца:" + e.getMessage());
        }

        log.info("{}[32m ==> GET /user by id={} <=={}[37m", (char) 27, id, (char) 27);
        return itemClient.getById(ownerId, id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getSelection(@RequestHeader("X-Sharer-User-Id") String userStr,
                                               @RequestParam(defaultValue = "") String text) {
        long userId;
        try {
            userId = Long.parseLong(userStr);
        } catch (NumberFormatException e) {
            log.warn("{}[31m ==> GET/items/search (METHOD getSelection) <== catch except: {} {}[37m",
                    (char) 27, e.getMessage() + e.getCause(), (char) 27);
            throw new BadRequestException("ошибка преобразования Id владельца:" + e.getMessage());
        }

        log.info("{}[32m ==> GET /items/search Item with text='{}' <=={}[37m", (char) 27, text, (char) 27);
        return itemClient.getSelection(userId, text);
    }

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader("X-Sharer-User-Id") String ownerStr,
                                      @RequestBody @Validated(Marker.Create.class) ItemDto itemDto) {
        long ownerId;
        try {
            ownerId = Long.parseLong(ownerStr);
        } catch (NumberFormatException e) {
            log.warn("{}[31m ==> POST/items (METHOD add) <== catch except: {} {}[37m",
                    (char) 27, e.getMessage() + e.getCause(), (char) 27);
            throw new BadRequestException("ошибка преобразования Id владельца:" + e.getMessage());
        }

        log.info("{}[32m ==> POST item: ownerId={}, item={}{}[37m", (char) 27, ownerId, itemDto, (char) 27);
        return itemClient.add(ownerId, itemDto);
    }

    @PostMapping("{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") String author,
                                             @PathVariable(required = false) Long itemId,
                                             @RequestBody @Valid CommentCreationDto comment) {
        log.info("{}[32m ==> POST/items/{itemId}={}/comment from user.id={} with text='{}' <=={}[37m",
                (char) 27, itemId, author, comment.getText(), (char) 27);
        Long authorId = Long.parseLong(author);
        comment.setItemId(itemId);
        comment.setAuthorId(authorId);
        return itemClient.addComment(comment);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") String ownerStr,
                                         @RequestBody @Validated(Marker.Update.class) ItemDto itemDto,
                                         @PathVariable(required = false) Long itemId) {
        long ownerId;
        try {
            ownerId = Long.parseLong(ownerStr);
        } catch (NumberFormatException e) {
            log.warn("{}[31m ==> PATCH/items/{itemId} (METHOD update) <== catch except: {} {}[37m",
                    (char) 27, e.getMessage() + e.getCause(), (char) 27);
            throw new BadRequestException("ошибка преобразования Id владельца:" + e.getMessage());
        }
        log.info("{}[32m ==>PATCH Update item: {} <== STARTED{}[37m", (char) 27, itemDto.getName(), (char) 27);
        if (itemId != null) {
            itemDto.setId(itemId);
        }
        return itemClient.update(ownerId, itemDto);
    }

}
