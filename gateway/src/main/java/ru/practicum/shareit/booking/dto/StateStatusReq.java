package ru.practicum.shareit.booking.dto;

import java.util.Optional;

public enum StateStatusReq {
    ALL,
    CURRENT,  // бронирования которые подтверждены владельцем вещи
    WAITING, // бронирования ожидающие подтверждения
    REJECTED, // бронирования отклоненные владельцем вещи
    PAST, // завершенные бронирования (неактуально)
    FUTURE;  // будущие бронирования

    public static Optional<StateStatusReq> from(String stringState) {
        for (StateStatusReq state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
