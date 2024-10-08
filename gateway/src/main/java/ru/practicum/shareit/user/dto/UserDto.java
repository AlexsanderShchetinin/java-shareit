package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.validator.Marker;

@Getter
@Setter
@Builder
public class UserDto {

    @Null(groups = Marker.Create.class, message = "При создании пользователя id должно быть null.")
    private Long id;

    @NotBlank(groups = Marker.Create.class, message = "Имя пользователя не может быть пустым")
    private String name;

    @Email(groups = Marker.Create.class,
            message = "email не соответствует стандарту электронного почтового адреса")
    @Email(groups = Marker.Update.class,
            message = "email не соответствует стандарту электронного почтового адреса")
    @NotNull(groups = Marker.Create.class, message = "Email пользователя не может быть пустым")
    private String email;

}
