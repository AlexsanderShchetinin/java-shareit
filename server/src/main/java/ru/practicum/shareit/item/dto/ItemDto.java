package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created Shchetinin Alexander
 */

@Builder
@Getter
@Setter
@ToString
public class ItemDto {

    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;  // поле заполняется, если Item создан в ответ на запрос (ItemRequest)

}
