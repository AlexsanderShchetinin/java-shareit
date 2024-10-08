package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by Shchetinin Alexander 03.10.2024
 */

@Builder
@Getter
@Setter
@ToString
public class ItemRequestDto {

    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemResponseDto> items;

}
