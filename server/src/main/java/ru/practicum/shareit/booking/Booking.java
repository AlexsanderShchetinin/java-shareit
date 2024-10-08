package ru.practicum.shareit.booking;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

/**
 * Created Shchetinin Alexander
 */

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bookings")
@ToString(of = "id")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_booking")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime start;

    @Column(name = "finish_booking")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime end;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status")
    private BookingStatus status;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item bookingItem;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User bookingAuthor;

}
