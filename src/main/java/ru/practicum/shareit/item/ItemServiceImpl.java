package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.InterruptionRuleException;
import ru.practicum.shareit.exception.MyNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;


    @Override
    public ItemDto add(String owner, ItemDto itemDto) {
        long ownerId = Long.parseLong(owner);
        userRepository.get(ownerId)
                .orElseThrow(
                        () -> new MyNotFoundException("Владелец с id=" + ownerId + " не зарегистрирован в приложении"));
        Item item = ItemMapper.toCreatedItem(itemDto);
        item.setOwner(ownerId);
        return ItemMapper.toDto(itemRepository.create(item));
    }

    @Override
    public ItemDto update(String owner, ItemDto itemDto) {
        long ownerId = Long.parseLong(owner);
        userRepository.get(ownerId)
                .orElseThrow(
                        () -> new MyNotFoundException("Владелец с id=" + ownerId + " не зарегистрирован в приложении"));
        if (itemDto.getId() == null) {
            throw new MyNotFoundException("Не передан идентификатор Вещи для обновления");
        }
        Item returnedItem = itemRepository.get(itemDto.getId())
                .orElseThrow(() -> new MyNotFoundException("Вещи с id=" + itemDto.getId() + " не существует."));
        if (returnedItem.getOwner() != ownerId) {
            throw new InterruptionRuleException("Редактировать Вещь может только её владелец");
        }

        // Если при обновлении передаются не все поля, то отсутствующие поля берем из репозитория
        if (itemDto.getName() == null || itemDto.getName().isEmpty()) {
            itemDto.setName(returnedItem.getName());
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isEmpty()) {
            itemDto.setDescription(returnedItem.getDescription());
        }
        if (itemDto.getAvailable() == null) {
            itemDto.setAvailable(returnedItem.isAvailable());
        }

        Item item = ItemMapper.toUpdatedItem(itemDto);
        item.setOwner(ownerId);
        Item updatedItem = itemRepository.update(item);
        return ItemMapper.toDto(updatedItem);
    }

    @Override
    public ItemDto getById(String owner, long itemId) {
        long ownerId = Long.parseLong(owner);
        userRepository.get(ownerId)
                .orElseThrow(
                        () -> new MyNotFoundException("Владелец с id=" + ownerId + " не зарегистрирован в приложении"));

        Item returnedItem = itemRepository.get(itemId)
                .orElseThrow(() -> new MyNotFoundException("Вещи с id=" + itemId + " не существует."));

        return ItemMapper.toDto(returnedItem);
    }

    @Override
    public List<ItemDto> getAllByOwner(String owner) {
        long ownerId = Long.parseLong(owner);
        return itemRepository.getAllByOwner(ownerId).stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public List<ItemDto> getSelection(String searchText) {
        return itemRepository.getSelection(searchText).stream()
                .map(ItemMapper::toDto)
                .toList();
    }
}
