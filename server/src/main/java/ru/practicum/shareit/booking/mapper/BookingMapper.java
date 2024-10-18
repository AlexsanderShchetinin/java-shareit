package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingCreatingDto;
import ru.practicum.shareit.booking.dto.BookingDto;

@Mapper(componentModel = "spring", uses = {BookingItemMapper.class, BookingUserMapper.class})
public interface BookingMapper {

    @Mapping(target = "bookingItem", source = "itemId", qualifiedByName = "toItem")
    Booking toModel(BookingCreatingDto bookingCreatingDto);

    @Mapping(target = "itemId", source = "bookingItem.id")
    BookingCreatingDto toCreatingDto(Booking booking);

    @Mapping(target = "item", source = "bookingItem")
    @Mapping(target = "booker", source = "bookingAuthor")
    BookingDto toDto(Booking booking);
}



