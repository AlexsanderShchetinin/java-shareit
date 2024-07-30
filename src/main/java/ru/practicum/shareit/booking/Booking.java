package ru.practicum.shareit.booking;

import lombok.*;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDate;

/**
 * Created Shchetinin Alexander
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Booking {

    private Item bookingItem;
    private LocalDate startBooking;
    private LocalDate finishBooking;
    private BookingStatus status;

}
