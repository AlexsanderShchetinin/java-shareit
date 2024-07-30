package ru.practicum.shareit.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.practicum.shareit.validator.Marker;

/**
 * Created Shchetinin Alexander
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {

    private Long id;

    private String name;

    @NotBlank(groups = Marker.Create.class, message = "Email пользователя не может быть пустым")
    private String email;

}
