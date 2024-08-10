package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreatingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStatusDto;
import ru.practicum.shareit.exception.InterruptionRuleException;

import java.util.List;

/**
 * Created Shchetinin Alexander 01.08.2024
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService service;

    // Добавление бронирования
    @PostMapping
    public ResponseEntity<BookingDto> addBooking(@RequestHeader("X-Sharer-User-Id") String ownerId,
                                                 @RequestBody @Validated BookingCreatingDto booking) {
        log.info("{}[34m==> POST/bookings <== TRY TO ADD NEW BOOKING {}", (char) 27, booking);
        BookingDto returnedBooking = service.create(ownerId, booking);
        log.info("{}[34m==> POST/bookings <== ADD NEW BOOKING {} COMPLETE ", (char) 27, returnedBooking);
        return ResponseEntity.status(HttpStatus.CREATED).body(returnedBooking);
    }

    // Изменение статуса по бронированию. Изменять статус может только владелец вещи
    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> changeStatus(@RequestHeader("X-Sharer-User-Id") String ownerId,
                                                   @PathVariable long bookingId,
                                                   @RequestParam(defaultValue = "unknown") String approved) {
        log.info("{}[34m==> PATCH bookings/{}?approved={} <== START REQUEST", (char) 27, bookingId, approved);
        if (!approved.equalsIgnoreCase("true") && !approved.equalsIgnoreCase("false")) {
            throw new InterruptionRuleException("Некорректно указан параметр approved в URL запроса");
        }
        BookingStatusDto bookingStatusDto = BookingStatusDto.builder()
                .id(bookingId)
                .approve(Boolean.parseBoolean(approved))
                .build();
        BookingDto returnedBooking = service.changeStatus(ownerId, bookingStatusDto);
        log.info("{}[34m==> PATCH bookings/{}?approved={} <== FINISH REQUEST", (char) 27, bookingId, approved);
        return ResponseEntity.ok().body(returnedBooking);
    }

    // получение бронирования по id
    @GetMapping("{bookingId}")
    public ResponseEntity<BookingDto> getById(@RequestHeader("X-Sharer-User-Id") String ownerId,
                                              @PathVariable long bookingId) {
        log.info("{}[34m==> GET bookings/{} <==", (char) 27, bookingId);
        return ResponseEntity.ok().body(service.getById(ownerId, bookingId));
    }

    // Получение списка всех бронирований текущего пользователя
    @GetMapping
    public ResponseEntity<List<BookingDto>> getBookingsByUser(@RequestHeader("X-Sharer-User-Id") String bookerStr,
                                                              @RequestParam(defaultValue = "ALL") String state) {
        log.info("{}[34m==> GET bookings?state={} <==", (char) 27, state);
        return ResponseEntity.ok().body(service.getBookingsByBooker(bookerStr, state));
    }

    // Получение списка бронирований для всех вещей текущего пользователя. Для владельца хотя бы одной вещи
    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getBookingsByOwner(@RequestHeader("X-Sharer-User-Id") String ownerId,
                                                               @RequestParam(defaultValue = "ALL") String state) {
        log.info("{}[34m==> GET bookings/owner?state={} <==", (char) 27, state);
        return ResponseEntity.ok().body(service.getBookingsByOwner(ownerId, state));
    }
}
