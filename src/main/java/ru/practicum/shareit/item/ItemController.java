package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validator.Marker;

import java.util.Collection;
import java.util.List;

/**
 * Created Shchetinin Alexander
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public Collection<ItemDto> getAllByOwner(@RequestHeader("X-Sharer-User-Id") String ownerId) {
        log.info("==> GET /all items for owner with id={}<==", ownerId);
        return itemService.getAllByOwner(ownerId);
    }

    @GetMapping("/{id}")
    public ItemDto getById(@RequestHeader("X-Sharer-User-Id") String ownerId, @PathVariable long id) {
        log.info("==> GET /user by id={} <==", id);
        return itemService.getById(ownerId, id);
    }

    @PostMapping
    public ItemDto add(@RequestHeader("X-Sharer-User-Id") String ownerId,
                       @RequestBody @Validated(Marker.Create.class) ItemDto itemDto) {
        log.info("==> POST item: ownerId={}, item={}", ownerId, itemDto);
        return itemService.add(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") String ownerId,
                          @RequestBody @Validated(Marker.Update.class) ItemDto itemDto,
                          @PathVariable(required = false) Long itemId) {
        log.info("PATCH ==> Update item: {} <== STARTED", itemDto.getName());
        if (itemId != null) {
            itemDto.setId(itemId);
        }
        ItemDto updatedItem = itemService.update(ownerId, itemDto);
        log.info("Item {}  with id={} UPDATED.", itemDto.getName(), itemDto.getId());
        return updatedItem;
    }

    @GetMapping("/search")
    public Collection<ItemDto> getSelection(@RequestParam(defaultValue = "") String text) {
        log.info("==> GET /search Item with text='{}' <==", text);
        List<ItemDto> returnedItems = itemService.getSelection(text);
        log.info("Get list items; size={}", returnedItems.size());
        return returnedItems;
    }


}
