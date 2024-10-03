package ru.practicum.shareit.item;

import jakarta.validation.Valid;
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
 * Created Shchetinin Alexander 24.07.2024
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<Collection<ItemBookTimeDto>> getAllByOwner(@RequestHeader("X-Sharer-User-Id") String ownerId) {
        log.info("==> GET /all items for owner with id={}<==", ownerId);
        return ResponseEntity.ok().body(itemService.getAllByOwner(ownerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemBookTimeDto> getById(@RequestHeader("X-Sharer-User-Id") String ownerId,
                                                   @PathVariable long id) {
        log.info("==> GET /user by id={} <==", id);
        return ResponseEntity.ok().body(itemService.getById(ownerId, id));
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<ItemDto>> getSelection(@RequestParam(defaultValue = "") String text) {
        log.info("==> GET /search Item with text='{}' <==", text);
        List<ItemDto> returnedItems = itemService.getSelection(text);
        log.info("Get list items by search={}; size={}", text, returnedItems.size());
        return ResponseEntity.ok().body(returnedItems);
    }

    @PostMapping
    public ResponseEntity<ItemDto> add(@RequestHeader("X-Sharer-User-Id") String ownerId,
                                       @RequestBody @Validated(Marker.Create.class) ItemDto itemDto) {
        log.info("==> POST item: ownerId={}, item={}", ownerId, itemDto);
        return ResponseEntity.ok().body(itemService.add(ownerId, itemDto));
    }

    @PostMapping("{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(@RequestHeader("X-Sharer-User-Id") String author,
                                                 @PathVariable(required = false) Long itemId,
                                                 @RequestBody @Valid CommentCreationDto comment) {
        log.info("==> POST/items/{itemId}={}/comment from user.id={} with text='{}' <==",
                itemId, author, comment.getText());
        Long authorId = Long.parseLong(author);
        comment.setItemId(itemId);
        comment.setAuthorId(authorId);
        CommentDto newComment = itemService.addComment(comment);
        log.info("==> COMPLETED POST/items/{itemId}={}/comment. ADDED COMMENT <==",
                itemId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> update(@RequestHeader("X-Sharer-User-Id") String ownerId,
                                          @RequestBody @Validated(Marker.Update.class) ItemDto itemDto,
                                          @PathVariable(required = false) Long itemId) {
        log.info("==>PATCH Update item: {} <== STARTED", itemDto.getName());
        if (itemId != null) {
            itemDto.setId(itemId);
        }
        ItemDto updatedItem = itemService.update(ownerId, itemDto);
        log.info("Item {}  with id={} UPDATED.", itemDto.getName(), itemDto.getId());
        return ResponseEntity.ok().body(updatedItem);
    }

}
