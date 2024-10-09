package ru.practicum.shareit.request.resp;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "item_responses")
public class ItemResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private ItemRequest request;

    @Column(name = "text_response")
    private String textResponse;

    @OneToOne
    @JoinColumn(name = "item_id")
    private Item item;

}

