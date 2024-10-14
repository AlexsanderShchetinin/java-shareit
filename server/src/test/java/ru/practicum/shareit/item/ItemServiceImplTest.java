package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {

    private final EntityManager em;
    private final ItemServiceImpl service;
    private final UserServiceImpl userService;
    private final ItemRepository repository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        repository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    void getAllByOwner() {
        // задаем параметры для теста, влияющие на наполнение БД данными
        final int AMOUNT_USER = 5;
        final int AMOUNT_ITEM = 5;

        // добавляем в БД новых пользователей
        List<UserDto> dtoUsers = makeUsersDto("SomeUser", "SomeEmail@ya.ru", AMOUNT_USER);

        List<Long> userIds = new ArrayList<>();
        for (UserDto dtoUser : dtoUsers) {
            userIds.add(userService.create(dtoUser).getId());
        }

        // добавляем несколько Item для каждого пользователя
        for (Long userId : userIds) {
            List<ItemDto> itemDtoList = makeItems(
                    "вещичка", "очень полезная и нужная", userId, AMOUNT_ITEM);

            // добавлем все вещи одного пользователя в БД
            for (ItemDto itemDto : itemDtoList) {
                service.add(userId.toString(), itemDto);
            }

            // запрос в ДБ на получение всех записей item по определенному пользователю
            TypedQuery<Item> updQuery1 = em.createQuery(
                    "SELECT i " +
                            "FROM Item AS i " +
                            "JOIN i.owner AS u " +
                            "WHERE u.id = :id", Item.class);
            List<Item> itemsFromDB = updQuery1.setParameter("id", userId).getResultList();

            // проверка количества записей в БД, также проверка первой и последней записи на наличие корректного владельца
            assertThat(itemsFromDB.size(), equalTo(itemDtoList.size()));
            assertThat(itemsFromDB.getFirst().getOwner().getId(), equalTo(userId));
            assertThat(itemsFromDB.getLast().getOwner().getId(), equalTo(userId));

            // проверка каждой вещи
            for (Item item : itemsFromDB) {

                TypedQuery<Item> updQuery2 = em.createQuery(
                        "SELECT i FROM Item AS i WHERE i.id = :id", Item.class);
                Item itemFromDB = updQuery2.setParameter("id", item.getId()).getSingleResult();

                assertThat(itemFromDB.getId(), notNullValue());
                assertThat(itemFromDB.getName(), equalTo(item.getName()));
                assertThat(itemFromDB.getDescription(), equalTo(item.getDescription()));
                assertThat(itemFromDB.getOwner().getId(), equalTo(userId));
                assertThat(itemFromDB.isAvailable(), equalTo(false));

                // меняем доступность и обновляем item
                ItemDto itemForUpd = ItemDto.builder()
                        .id(itemFromDB.getId())
                        .available(true)
                        .build();
                service.update(userId.toString(), itemForUpd);

                // заново получаем эту же запись
                TypedQuery<Item> updQuery3 = em.createQuery(
                        "SELECT i FROM Item AS i WHERE i.id = :id", Item.class);
                Item updItemFromDB = updQuery3.setParameter("id", item.getId()).getSingleResult();

                // проверка доступности item
                assertThat(updItemFromDB.getId(), notNullValue());
                assertThat(updItemFromDB.getName(), equalTo(item.getName()));
                assertThat(updItemFromDB.getDescription(), equalTo(item.getDescription()));
                assertThat(updItemFromDB.getOwner().getId(), equalTo(userId));
                assertThat(updItemFromDB.isAvailable(), equalTo(true));
            }
        }

    }


    private List<ItemDto> makeItems(String name, String description, long userId, int amountItem) {
        List<ItemDto> items = new ArrayList<>();
        for (int i = 0; i < amountItem; i++) {
            items.add(ItemDto.builder()
                    .name(name + "_номер " + i + " владельца с id=" + userId)
                    .description(description + "_номер " + i + " владельца с id=" + userId)
                    .build());
        }
        return items;
    }

    private List<UserDto> makeUsersDto(String beginName, String endEmail, int amountUsers) {
        List<UserDto> users = new ArrayList<>();
        for (int i = 0; i < amountUsers; i++) {
            users.add(UserDto.builder()
                    .name(beginName + "_" + i)
                    .email("test_" + i + "_" + endEmail)
                    .build());
        }
        return users;
    }

}