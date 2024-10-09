package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.resp.ItemResponse;

@Mapper(componentModel = "spring")
public interface ItemResponseMapper {

    ItemResponse toModel(ItemResponseDto itemResponseDto);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "name", source = "item.name")
    @Mapping(target = "ownerId", source = "item.owner.id")
    ItemResponseDto toDto(ItemResponse itemResponse);

}
