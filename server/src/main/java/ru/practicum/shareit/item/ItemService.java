package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentCreationDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookTimeDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto add(String ownerId, ItemDto itemDto);

    ItemDto update(String ownerId, ItemDto itemDto);

    ItemBookTimeDto getById(String ownerId, long itemId);

    List<ItemBookTimeDto> getAllByOwner(String ownerId);

    List<ItemDto> getSelection(String searchText);

    CommentDto addComment(CommentCreationDto comment);

}
