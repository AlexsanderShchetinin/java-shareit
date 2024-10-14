package ru.practicum.shareit.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.DuplicatedException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.resp.ItemResponseRepository;
import ru.practicum.shareit.user.dto.UserDto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplTest {

    private final EntityManager em;
    private final UserServiceImpl service;
    private final UserRepository repository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemReqRepository;
    private final ItemResponseRepository itemRespRepository;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        itemRespRepository.deleteAll();
        itemReqRepository.deleteAll();
        repository.deleteAll();
    }

    @Test
    void update() {

        // первоначально сохраняем в БД пользователя
        UserDto userDto = makeUserDto("Kim", "Moldabaeva.K@mail.ru");
        UserDto returnUser = service.create(userDto);

        // проверили, что сохранение в БД прошло корректно
        assertThat(returnUser.getId(), notNullValue());
        assertThat(returnUser.getName(), equalTo(userDto.getName()));
        assertThat(returnUser.getEmail(), equalTo(userDto.getEmail()));

        // 1. корректное обновление:
        // 1.1. имя (email делаем null)
        userDto.setName("Alex");
        userDto.setEmail(null);
        service.update(returnUser.getId(), userDto);
        TypedQuery<User> updQuery1 = em.createQuery("SELECT u FROM User AS u WHERE u.id = :id", User.class);
        User user1 = updQuery1.setParameter("id", returnUser.getId()).getSingleResult();

        assertThat(user1.getId(), notNullValue());
        assertThat(user1.getName(), equalTo(userDto.getName()));
        assertThat(user1.getEmail(), equalTo("Moldabaeva.K@mail.ru"));

        // 1.2. email (имя делаем пустым)
        userDto.setName("");
        userDto.setEmail("shch@ya.ru");
        service.update(returnUser.getId(), userDto);
        TypedQuery<User> updQuery2 = em.createQuery("SELECT u FROM User AS u WHERE u.id = :id", User.class);
        User user2 = updQuery2.setParameter("id", returnUser.getId()).getSingleResult();

        assertThat(user2.getId(), notNullValue());
        assertThat(user2.getName(), equalTo("Alex"));
        assertThat(user2.getEmail(), equalTo(userDto.getEmail()));

        // 2. некорректное обновление:
        // 2.1. пытаемся обновить email, когда в БД уже существует пользователь с таким email
        // добавляем еще одного пользователя
        UserDto userDto2 = makeUserDto("Kim", "Moldabaeva.K@mail.ru");
        service.create(userDto2);

        userDto.setEmail("Moldabaeva.K@mail.ru");
        try {
            service.update(returnUser.getId(), userDto);  // тут обновление записи в БД произойти не должно
        } catch (DuplicatedException e) {
            TypedQuery<User> updQuery3 = em.createQuery("SELECT u FROM User AS u WHERE u.id = :id", User.class);
            User user3 = updQuery3.setParameter("id", returnUser.getId()).getSingleResult();

            assertThat(user3.getId(), notNullValue());
            assertThat(user3.getName(), equalTo("Alex"));
            assertThat(user3.getEmail(), equalTo("shch@ya.ru")); // проверяем что email остался прежним

        }

    }


    private UserDto makeUserDto(String name, String email) {
        return UserDto.builder()
                .name(name)
                .email(email)
                .build();
    }


}