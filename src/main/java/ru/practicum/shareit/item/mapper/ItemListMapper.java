package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(componentModel = "spring", uses = ItemMapper.class)
public interface ItemListMapper {

    List<Item> toListModel(List<ItemDto> itemListDto);

    List<ItemDto> toListDto(List<Item> itemList);

}
