package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Created Shchetinin Alexander
 */
@Getter
@Setter
@Builder
@ToString
public class BookingCreatingDto {

    @NotNull(message = "В бронировании поле с идентификатором itemId не может быть пустым.")
    private Long itemId;

    @FutureOrPresent(message = "Дата начала бронирования не может быть в прошлом.")
    @NotNull(message = "В бронировании поле с датой начала не может быть пустым.")
    private LocalDateTime start;

    @Future(message = "Дата окончания бронирования должна быть в будующем.")
    @NotNull(message = "В бронировании поле с датой окончания не может быть пустым.")
    private LocalDateTime end;

}
