package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.resp.ItemResponse;

import java.util.List;

@Mapper(componentModel = "spring", uses = ItemResponseMapper.class)
public interface ItemResponseListMapper {

    List<ItemResponse> toListModel(List<ItemResponseDto> itemRespListDto);

    List<ItemResponseDto> toListDto(List<ItemResponse> itemRespList);
}
