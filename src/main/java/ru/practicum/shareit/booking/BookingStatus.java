package ru.practicum.shareit.booking;

public enum BookingStatus {

    REQUESTED, // запрос на бронирование отправлен пользователем
    CONFIRMED, // запрос на бронирование одобрен владельцем вещи
    REJECTED, // запрос на бронирование отклонен владельцем вещи
    COMPLETED // бронирование завершено (неактуально)

}
