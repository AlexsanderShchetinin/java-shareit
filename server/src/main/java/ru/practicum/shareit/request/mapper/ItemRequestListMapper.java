package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.resp.ItemResponse;

import java.util.List;

@Mapper(componentModel = "spring", uses = ItemRequestMapper.class)
public interface ItemRequestListMapper {

    List<ItemRequest> toListModel(List<ItemRequestDto> itemReqListDto);

    List<ItemRequestDto> toListDto(List<ItemRequest> itemReqList);
}
