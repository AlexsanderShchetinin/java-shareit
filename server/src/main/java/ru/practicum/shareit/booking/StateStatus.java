package ru.practicum.shareit.booking;

public enum StateStatus {
    ALL,
    CURRENT,  // бронирования которые подтверждены владельцем вещи
    WAITING, // бронирования ожидающие подтверждения
    REJECTED, // бронирования отклоненные владельцем вещи
    PAST, // завершенные бронирования (неактуально)
    FUTURE  // будущие бронирования
}
