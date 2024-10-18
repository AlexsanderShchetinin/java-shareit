package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingServiceImpl;
import ru.practicum.shareit.booking.dto.BookingCreatingDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.item.dto.CommentCreationDto;
import ru.practicum.shareit.item.dto.ItemBookTimeDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {

    private final EntityManager em;
    private final ItemServiceImpl service;
    private final UserServiceImpl userService;
    private final BookingServiceImpl bookingService;
    private final ItemRepository repository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final List<Long> userIds = new ArrayList<>();
    // задаем параметры для тестов, влияющие на наполнение БД данными (заполнять так чтобы произведение было четным)
    private final int amountUser = 2;
    private final int amountItem = 5;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        commentRepository.deleteAll();
        repository.deleteAll();
        userRepository.deleteAll();

        // добавляем в БД новых пользователей
        List<UserDto> dtoUsers = makeUsersDto("SomeUser", "SomeEmail@ya.ru", amountUser);
        for (UserDto dtoUser : dtoUsers) {
            userIds.add(userService.create(dtoUser).getId());
        }
    }

    @Test
    void getAllByOwner() {

        // добавляем несколько Item для каждого пользователя
        for (Long userId : userIds) {
            List<ItemDto> itemDtoList = makeItems(
                    "вещичка", "очень полезная и нужная", userId, amountItem);

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

    @Test
    void getById() {

        // добавляем несколько Item для каждого пользователя
        for (Long userId : userIds) {
            List<ItemDto> itemDtoList = makeItems(
                    "Айтем", "тест получения айтем по айди", userId, amountItem);

            // добавлем и получаем все вещи одного пользователя в БД
            for (ItemDto itemDto : itemDtoList) {
                ItemDto itemDto1 = service.add(userId.toString(), itemDto);

                ItemBookTimeDto returnItem = service.getById(userId.toString(), itemDto1.getId());

                TypedQuery<Item> updQuery = em.createQuery(
                        "SELECT i FROM Item AS i WHERE i.id = :id", Item.class);
                Item resp = updQuery.setParameter("id", itemDto1.getId()).getSingleResult();

                // проверка item
                assertThat(resp.getId(), notNullValue());
                assertThat(resp.getName(), equalTo(returnItem.getName()));
                assertThat(resp.getDescription(), equalTo(returnItem.getDescription()));
                assertThat(resp.getOwner().getId(), equalTo(userId));
            }
        }
    }

    @Test
    void getSelection() {
        // добавляем нужные Item для каждого пользователя
        for (Long userId : userIds) {
            List<ItemDto> itemDtoList = makeAvailableItems(
                    "For selection", "тест поиска айтемов", userId, amountItem);
            for (ItemDto itemDto : itemDtoList) {
                service.add(userId.toString(), itemDto);
            }
        }

        // добавляем Item для каждого пользователя но которые не доступны (available=false)
        for (Long userId : userIds) {
            List<ItemDto> itemDtoList = makeItems(
                    "For selection", "тест поиска айтемов", userId, amountItem);
            for (ItemDto itemDto : itemDtoList) {
                service.add(userId.toString(), itemDto);
            }
        }

        // добавляем вспомагательные Item для каждого пользователя
        for (Long userId : userIds) {
            List<ItemDto> itemDtoList = makeItems(
                    "Второстепенный айтем", "для разнообразия", userId, amountItem);
            for (ItemDto itemDto : itemDtoList) {
                service.add(userId.toString(), itemDto);
            }
        }

        // возвращаем из БД 3 списка с учетом available=false
        // (первые два должны быть с одинаковыми айтемами и одинаковой последовательностью,
        // третий должен быть пустым из-за пустого текста)
        List<ItemDto> select1 = service.getSelection("selection");
        List<ItemDto> select2 = service.getSelection("пОисК");
        List<ItemDto> select3 = service.getSelection("");

        // проверка
        assertThat(select1.size(), equalTo(select2.size()));
        assertThat(select1.size(), is(amountUser * amountItem));
        assertThat(select1.getFirst().getId(), equalTo(select2.getFirst().getId()));
        assertThat(select1.getLast().getId(), equalTo(select2.getLast().getId()));
        assertThat(select3.toArray(), is(emptyArray()));
    }

    @Test
    void addComment() {
        List<ItemDto> userItems = new ArrayList<>();
        List<Long> bookUsers = new ArrayList<>();
        List<Long> ownerUsers = new ArrayList<>();
        // добавляем несколько Item для каждого пользователя
        int i = 0;
        for (Long userId : userIds) {
            i++;
            // половина пользователей владельцы item
            if (userIds.size() / 2 >= i) {

                List<ItemDto> itemDtoList = makeAvailableItems(
                        "Айтем для коммента", "дабы добавить comment сюда", userId, amountItem);

                // добавлем  все вещи одного пользователя в БД
                for (ItemDto itemDto : itemDtoList) {
                    userItems.add(service.add(userId.toString(), itemDto));
                }
                ownerUsers.add(userId);  // запоминаем id пользователей, которые создавали вещи
            } else {  // остальная половина бронируют созданные вещи
                for (ItemDto userItem : userItems) {
                    BookingCreatingDto bookingCreatingDto = makeNewBooking(userItem.getId(),
                            "2024-10-", "2024-10-", i);
                    bookingService.create(userId.toString(), bookingCreatingDto);
                }
                bookUsers.add(userId);  // запоминаем id пользователей, которые бронировали вещи
            }
            // добавляем комментарии
            for (ItemDto itemDto : userItems) {
                for (Long bookUser : bookUsers) {
                    i++;
                    service.addComment(makeComment(itemDto.getId(), bookUser, i));
                }
            }

            for (Long bookUser : bookUsers) {
                // проверяем в БД кол-во комментов к каждой вещи
                TypedQuery<Comment> updQuery = em.createQuery(
                        "SELECT c FROM Comment AS c " +
                                "JOIN c.author AS u " +
                                "WHERE c.author.id = :id", Comment.class);
                List<Comment> resp = updQuery.setParameter("id", bookUser).getResultList();

                assertThat(resp.size(), is(amountUser * amountItem / 2));
            }

            // пытаемся добавить комментарий, не забронировав вещь
            try {
                for (ItemDto itemDto : userItems) {
                    for (Long bookUser : ownerUsers) {
                        i++;
                        service.addComment(makeComment(itemDto.getId(), bookUser, i));
                    }
                }
                // проверяем что общее кол-во комментов не изменилось в БД (если дойдет до этого)
                TypedQuery<Comment> updQuery = em.createQuery(
                        "SELECT c FROM Comment AS c " +
                                "JOIN c.author AS u ", Comment.class);
                List<Comment> respAll = updQuery.getResultList();

                assertThat(respAll.size(), is(amountUser * amountItem * bookUsers.size() / 2));
            } catch (BadRequestException e) {
                // DO NOTHING, JUST CATCH
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

    private List<ItemDto> makeAvailableItems(String name, String description, long userId, int amountItem) {
        List<ItemDto> items = new ArrayList<>();
        for (int i = 0; i < amountItem; i++) {
            items.add(ItemDto.builder()
                    .name(name + "_номер " + i + " владельца с id=" + userId)
                    .description(description + "_номер " + i + " владельца с id=" + userId)
                    .available(true)
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

    private CommentCreationDto makeComment(long itemId, long authorId, int prefix) {
        return CommentCreationDto.builder()
                .text(prefix + " - такие комментарии нам нужны!")
                .itemId(itemId)
                .authorId(authorId)
                .build();
    }

    // создание бронирования продолжительностью 3 дня
    private BookingCreatingDto makeNewBooking(Long itemId, String startDate, String endDate, int amount) {
        String dayStart = Integer.toString(amount);
        if (amount <= 9) {
            dayStart = "0" + dayStart;
        }
        String dayEnd = Integer.toString(amount + 3);
        if (amount + 3 <= 9) {
            dayEnd = "0" + dayEnd;
        }
        return BookingCreatingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.parse(startDate + dayStart + "T10:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .end(LocalDateTime.parse(endDate + dayEnd + "T20:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
}