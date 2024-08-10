package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.CommentCreationDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookTimeDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    Item toModel(ItemDto itemDto);

    ItemDto toDto(Item item);

    ItemBookTimeDto toBookingTimeDto(Item item);

    @Mapping(target = "authorName", source = "author.name")
    CommentDto toCommentDto(Comment comment);

    Comment toCommentModel(CommentCreationDto comment);

}
