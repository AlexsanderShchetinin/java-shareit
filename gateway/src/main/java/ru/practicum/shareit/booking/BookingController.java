package ru.practicum.shareit.booking;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreatingDto;
import ru.practicum.shareit.booking.dto.BookingStatusDto;
import ru.practicum.shareit.booking.dto.StateStatusReq;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.InterruptionRuleException;


@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    // Добавление бронирования
    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader("X-Sharer-User-Id") String ownerStr,
                                             @RequestBody @Validated BookingCreatingDto booking) {
        log.info("{}[32m ==> POST/bookings <== TRY TO ADD NEW BOOKING {}{}[37m", (char) 27, booking, (char) 27);
        try {
            long ownerId = Long.parseLong(ownerStr);
            return bookingClient.create(ownerId, booking);
        } catch (NumberFormatException e) {
            log.warn("{}[31m ==> POST/bookings catch except: {} {}[37m",
                    (char) 27, e.getMessage() + e.getCause(), (char) 27);
            throw new BadRequestException("ошибка преобразования Id владельца:" + e.getMessage());
        }
    }

    // Изменение статуса по бронированию. Изменять статус может только владелец вещи
    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> changeStatus(@RequestHeader("X-Sharer-User-Id") String ownerStr,
                                               @PathVariable long bookingId,
                                               @RequestParam(defaultValue = "unknown") String approved) {
        log.info("{}[32m ==> PATCH bookings/{}?approved={} <== START REQUEST{}[37m",
                (char) 27, bookingId, approved, (char) 27);
        long ownerId;
        try {
            ownerId = Long.parseLong(ownerStr);
        } catch (NumberFormatException e) {
            log.warn("{}[31m ==> PATCH/bookings/{bookingId} <== catch except: {} {}[37m",
                    (char) 27, e.getMessage() + e.getCause(), (char) 27);
            throw new BadRequestException("ошибка преобразования Id владельца:" + e.getMessage());
        }

        if (!approved.equalsIgnoreCase("true") && !approved.equalsIgnoreCase("false")) {
            throw new InterruptionRuleException("Некорректно указан параметр approved в URL запроса");
        }
        BookingStatusDto bookingStatusDto = BookingStatusDto.builder()
                .id(bookingId)
                .approve(Boolean.parseBoolean(approved))
                .build();
        return bookingClient.changeStatus(ownerId, bookingStatusDto);
    }

    // получение бронирования по id
    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") String ownerStr,
                                          @PathVariable long bookingId) {
        long ownerId;
        try {
            ownerId = Long.parseLong(ownerStr);
        } catch (NumberFormatException e) {
            log.warn("{}[31m ==> GET/bookings/{bookingId} <== catch except: {} {}[37m",
                    (char) 27, e.getMessage() + e.getCause(), (char) 27);
            throw new BadRequestException("ошибка преобразования Id владельца:" + e.getMessage());
        }
        log.info("{}[32m ==> GET bookings/{} <=={}[37m", (char) 27, bookingId, (char) 27);
        return bookingClient.getById(ownerId, bookingId);
    }

    // Получение списка бронирований для всех вещей текущего пользователя. Для владельца хотя бы одной вещи
    @GetMapping("/owner")
    public ResponseEntity<Object>
    getBookingsByOwner(@RequestHeader("X-Sharer-User-Id") String ownerStr,
                       @RequestParam(defaultValue = "ALL") String stateParam,
                       @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                       @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        long ownerId;
        try {
            ownerId = Long.parseLong(ownerStr);
        } catch (NumberFormatException e) {
            log.warn("{}[31m ==> GET/bookings/owner <== catch except: {} {}[37m",
                    (char) 27, e.getMessage() + e.getCause(), (char) 27);
            throw new BadRequestException("ошибка преобразования Id владельца:" + e.getMessage());
        }
        log.info("{}[32m ==> GET bookings/owner?state={} <=={}[37m", (char) 27, stateParam, (char) 27);
        StateStatusReq state = StateStatusReq.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getBookingsByOwner(ownerId, state, from, size);
    }

    // Получение списка всех бронирований текущего пользователя
    @GetMapping
    public ResponseEntity<Object>
    getBookings(@RequestHeader("X-Sharer-User-Id") String bookerStr,
                @RequestParam(name = "state", defaultValue = "all") String stateParam,
                @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {

        StateStatusReq state = StateStatusReq.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));

        long bookerId;
        try {
            bookerId = Long.parseLong(bookerStr);
        } catch (NumberFormatException e) {
            log.warn("{}[31m ==> GET/bookings <== catch except: {} {}[37m",
                    (char) 27, e.getMessage() + e.getCause(), (char) 27);
            throw new BadRequestException("ошибка преобразования Id владельца:" + e.getMessage());
        }

        log.info("{}[32m ==> GET bookings/?state={} <=={}[37m", (char) 27, stateParam, (char) 27);
        return bookingClient.getBookings(bookerId, state, from, size);
    }


}