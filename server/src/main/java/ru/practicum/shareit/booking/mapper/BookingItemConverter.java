package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface BookingItemConverter {

    @Named("toItem")
    @Mapping(target = "id", source = "id")
    Item toItem(Long id);

}