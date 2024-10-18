package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.shareit.user.User;

@Mapper(componentModel = "spring")
public interface BookingUserMapper {

    @Named("toUser")
    @Mapping(target = "id", source = "id")
    User toUser(Long id);

}