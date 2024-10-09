package ru.practicum.shareit.booking.dto;

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

    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;

}
