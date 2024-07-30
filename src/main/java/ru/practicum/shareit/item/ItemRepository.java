package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    Optional<Item> create(Item item);

    Optional<Item> update(Item item);

    List<Item> getAllByOwner(long ownerId);

    Optional<Item> get(long id);

    List<Item> getSelection(String searchText);

}
