package ru.practicum.shareit.request;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

/**
 * Created by Shchetinin Alexander 03.10.2024
 */

@Entity
@Table(name = "item_requests")
@Getter
@Setter
@NoArgsConstructor
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "request_owner_id")
    private User requestOwner;  // идентификатор пользователя, создавшего запрос.

    private String description;

    private LocalDateTime created;


}
