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
        log.info("{}[34m==> GET /all items for owner with id={}<==", (char) 27, ownerId);
        return ResponseEntity.ok().body(itemService.getAllByOwner(ownerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemBookTimeDto> getById(@RequestHeader("X-Sharer-User-Id") String ownerId,
                                                   @PathVariable long id) {
        log.info("{}[34m==> GET /user by id={} <==", (char) 27, id);
        return ResponseEntity.ok().body(itemService.getById(ownerId, id));
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<ItemDto>> getSelection(@RequestParam(defaultValue = "") String text) {
        log.info("{}[34m==> GET /search Item with text='{}' <==", (char) 27, text);
        List<ItemDto> returnedItems = itemService.getSelection(text);
        log.info("{}[34mGet list items by search={}; size={}", (char) 27, text, returnedItems.size());
        return ResponseEntity.ok().body(returnedItems);
    }

    @PostMapping
    public ResponseEntity<ItemDto> add(@RequestHeader("X-Sharer-User-Id") String ownerId,
                                       @RequestBody @Validated(Marker.Create.class) ItemDto itemDto) {
        log.info("{}[34m==> POST item: ownerId={}, item={}", (char) 27, ownerId, itemDto);
        return ResponseEntity.ok().body(itemService.add(ownerId, itemDto));
    }

    @PostMapping("{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(@RequestHeader("X-Sharer-User-Id") String author,
                                                 @PathVariable(required = false) Long itemId,
                                                 @RequestBody @Valid CommentCreationDto comment) {
        log.info("{}[34m==> POST/items/{itemId}={}/comment from user.id={} with text='{}' <==",
                (char) 27, itemId, author, comment.getText());
        Long authorId = Long.parseLong(author);
        comment.setItemId(itemId);
        comment.setAuthorId(authorId);
        CommentDto newComment = itemService.addComment(comment);
        log.info("{}[34m==> COMPLETED POST/items/{itemId}={}/comment. ADDED COMMENT <==",
                (char) 27, itemId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> update(@RequestHeader("X-Sharer-User-Id") String ownerId,
                                          @RequestBody @Validated(Marker.Update.class) ItemDto itemDto,
                                          @PathVariable(required = false) Long itemId) {
        log.info("{}[34m==>PATCH Update item: {} <== STARTED", (char) 27, itemDto.getName());
        if (itemId != null) {
            itemDto.setId(itemId);
        }
        ItemDto updatedItem = itemService.update(ownerId, itemDto);
        log.info("{}[34mItem {}  with id={} UPDATED.", (char) 27, itemDto.getName(), itemDto.getId());
        return ResponseEntity.ok().body(updatedItem);
    }

}
