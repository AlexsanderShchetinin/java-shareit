package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemResponseDto {

    private String name;
    private Long itemId;
    private Long ownerId;
    private String textResponse;
}
