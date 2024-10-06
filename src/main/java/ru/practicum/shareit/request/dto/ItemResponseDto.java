package ru.practicum.shareit.request.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.request.ItemRequest;

@Getter
@Setter
@NoArgsConstructor
public class ItemResponseDto {

    private String name;
    private Long itemId;
    private Long ownerId;
    private String textResponse;
}
