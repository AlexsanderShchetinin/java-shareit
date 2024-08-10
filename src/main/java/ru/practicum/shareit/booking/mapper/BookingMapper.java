package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingCreatingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

@Mapper(componentModel = "spring", uses = {BookingItemConverter.class, BookingUserConverter.class})
public interface BookingMapper {

    @Mapping(target = "bookingItem", source = "itemId", qualifiedByName = "toItem")
    Booking toModel(BookingCreatingDto bookingCreatingDto);

    @Mapping(target = "itemId", source = "bookingItem.id")
    BookingCreatingDto toCreatingDto(Booking booking);

    @Mapping(target = "item", source = "bookingItem")
    @Mapping(target = "booker", source = "bookingAuthor")
    BookingDto toDto(Booking booking);
}

@Mapper(componentModel = "spring")
interface BookingItemConverter {

    @Named("toItem")
    @Mapping(target = "id", source = "id")
    Item toItem(Long id);

}

@Mapper(componentModel = "spring")
interface BookingUserConverter {

    @Named("toUser")
    @Mapping(target = "id", source = "id")
    User toUser(Long id);

}
