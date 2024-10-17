package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

/**
 * Created Shchetinin Alexander
 */
@Getter
@Setter
@Builder
@ToString
public class BookingDto {

    private Long id;

    @FutureOrPresent(message = "Дата начала бронирования не может быть в прошлом.")
    @NotNull(message = "В бронировании поле с датой начала не может быть пустым.")
    private LocalDateTime start;

    @Future(message = "Дата окончания бронирования должна быть в будующем.")
    @NotNull(message = "В бронировании поле с датой окончания не может быть пустым.")
    private LocalDateTime end;

    private ItemDto item;
    private UserDto booker;
    private BookingStatus status;
}
