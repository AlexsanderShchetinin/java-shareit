package ru.practicum.shareit.booking;

public enum BookingStatus {

    WAITING, // запрос на бронирование отправлен пользователем
    APPROVED, // запрос на бронирование одобрен владельцем вещи
    REJECTED, // запрос на бронирование отклонен владельцем вещи
    COMPLETED, // бронирование завершено (неактуально)
    CANCELED  // бронирование отменено создателем

}
