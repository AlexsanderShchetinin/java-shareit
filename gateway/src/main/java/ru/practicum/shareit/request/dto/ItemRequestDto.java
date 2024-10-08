package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.validator.Marker;

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

    @Null(groups = Marker.Create.class, message = "При создании запроса id должно быть null.")
    private Long id;

    @NotBlank(message = "Запрос не может быть пустым")
    private String description;
    private LocalDateTime created;
    private List<ItemResponseDto> items;

}
