package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
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
public class CommentCreationDto {

    @NotBlank(message = "Текст комментария не может быть пустым.")
    private String text;

    private Long itemId;
    private Long authorId;

}
