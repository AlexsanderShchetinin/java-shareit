package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;

/**
 * Created Shchetinin Alexander
 */

@Builder
@Getter
@Setter
@ToString
public class CommentDto {

    private Long id;
    private String text;
    private Item item;
    private String authorName;
    private LocalDateTime created;

}
