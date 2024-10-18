package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Created Shchetinin Alexander
 */

@Builder
@Getter
@Setter
public class BookingStatusDto {

    private Long id;
    private Boolean approve;

}
