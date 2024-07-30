package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto add(String ownerId, ItemDto itemDto);

    ItemDto update(String ownerId, ItemDto itemDto);

    ItemDto getById(String ownerId, long itemId);

    List<ItemDto> getAllByOwner(String ownerId);

    List<ItemDto> getSelection(String searchText);

}
