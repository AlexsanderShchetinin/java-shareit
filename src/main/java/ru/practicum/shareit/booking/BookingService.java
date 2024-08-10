package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreatingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStatusDto;

import java.util.List;

/**
 * Created Shchetinin Alexander 01.08.2024
 */

public interface BookingService {

    BookingDto create(String ownerId, BookingCreatingDto bookingCreatingDto);

    BookingDto changeStatus(String ownerId, BookingStatusDto bookingStatusDto);

    BookingDto getById(String ownerId, long bookingId);

    List<BookingDto> getBookingsByBooker(String bookerStr, String state);

    List<BookingDto> getBookingsByOwner(String ownerId, String state);

}
