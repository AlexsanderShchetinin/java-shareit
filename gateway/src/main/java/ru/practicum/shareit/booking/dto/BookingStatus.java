package ru.practicum.shareit.booking.dto;

import java.util.Optional;

public enum BookingStatus {

    WAITING, // запрос на бронирование отправлен пользователем
    APPROVED, // запрос на бронирование одобрен владельцем вещи
    REJECTED, // запрос на бронирование отклонен владельцем вещи
    COMPLETED, // бронирование завершено (неактуально)
    CANCELED;  // бронирование отменено создателем

    public static Optional<BookingStatus> from(String stringState) {
        for (BookingStatus state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }

}
