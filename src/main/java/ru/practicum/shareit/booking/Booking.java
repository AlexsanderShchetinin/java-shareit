package ru.practicum.shareit.booking;

import lombok.*;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDate;

/**
 * Created Shchetinin Alexander
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Booking {

    Item bookingItem;
    LocalDate startBooking;
    LocalDate finishBooking;
    BookingStatus status;

}
