package ru.practicum.shareit.request;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemResponse;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by Shchetinin Alexander 03.10.2024
 */

@Entity
@Table(name = "itemRequests")
@Getter
@Setter
@NoArgsConstructor
public class ItemRequest {

    private long id;
    private String description;
    private List<ItemResponse> responses;
    private LocalDateTime created;


}
