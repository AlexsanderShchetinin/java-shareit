package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreationDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookTimeDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validator.Marker;

import java.util.Collection;
import java.util.List;

/**
 * Created by Shchetinin Alexander 24.07.2024
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<Collection<ItemBookTimeDto>> getAllByOwner(@RequestHeader("X-Sharer-User-Id") String ownerId) {
        log.info("{}[32m ==> GET /all items for owner with id={}<=={}[37m", (char) 27, ownerId, (char) 27);
        return ResponseEntity.ok().body(itemService.getAllByOwner(ownerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemBookTimeDto> getById(@RequestHeader("X-Sharer-User-Id") String ownerId,
                                                   @PathVariable long id) {
        log.info("{}[32m ==> GET /user by id={} <=={}[37m",(char) 27, id, (char) 27);
        return ResponseEntity.ok().body(itemService.getById(ownerId, id));
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<ItemDto>> getSelection(@RequestParam(defaultValue = "") String text) {
        List<ItemDto> returnedItems = itemService.getSelection(text);
        log.info("{}[32m Get list items by search={}; size={}{}[37m",(char) 27, text, returnedItems.size(),(char) 27);
        return ResponseEntity.ok().body(returnedItems);
    }

    @PostMapping
    public ResponseEntity<ItemDto> add(@RequestHeader("X-Sharer-User-Id") String ownerId,
                                       @RequestBody @Validated(Marker.Create.class) ItemDto itemDto) {
        log.info("{}[32m ==> POST item: ownerId={}, item={}{}[37m", (char) 27,ownerId, itemDto,(char) 27);
        return ResponseEntity.ok().body(itemService.add(ownerId, itemDto));
    }

    @PostMapping("{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(@RequestBody CommentCreationDto comment) {
        log.info("{}[32m ==> SERVER--> POST/items/{itemId}={}/comment. addComment <=={}[37m",
                (char) 27, comment.getItemId(), (char) 27);
        CommentDto newComment = itemService.addComment(comment);
        log.info("{}[32m ==> COMPLETED POST/items/{itemId}={}/comment. ADDED COMMENT <=={}[37m",
                (char) 27, comment.getItemId(), (char) 27);
        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> update(@RequestHeader("X-Sharer-User-Id") String ownerId,
                                          @RequestBody @Validated(Marker.Update.class) ItemDto itemDto,
                                          @PathVariable(required = false) Long itemId) {
        log.info("{}[32m ==>PATCH Update item: {} <== STARTED{}[37m", (char) 27, itemDto.getName(), (char) 27);
        if (itemId != null) {
            itemDto.setId(itemId);
        }
        ItemDto updatedItem = itemService.update(ownerId, itemDto);
        log.info("{}[32m Item {}  with id={} UPDATED.{}[37m", (char) 27, itemDto.getName(), itemDto.getId(), (char) 27);
        return ResponseEntity.ok().body(updatedItem);
    }

}
