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

    public Long id;
    public Boolean approve;

}
