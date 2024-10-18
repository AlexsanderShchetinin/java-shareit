package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String text; // тект комментария

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item; // вещь, к которой относится коммент

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author; // автор комментария

    private LocalDateTime created; // дата создания комментария

}
