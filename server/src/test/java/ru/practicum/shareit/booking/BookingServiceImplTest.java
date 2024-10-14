package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingCreatingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStatusDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.ItemBookTimeDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.resp.ItemResponseRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImplTest.class);
    private final EntityManager em;
    private final BookingRepository repository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemReqRepository;
    private final ItemResponseRepository itemRespRepository;
    private final UserServiceImpl userService;
    private final ItemServiceImpl itemService;
    private final BookingServiceImpl bookingService;


    @BeforeEach
    void setUp() {
        repository.deleteAll();
        itemRespRepository.deleteAll();
        itemReqRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getBookings() {

        // задаем параметры для теста, влияющие на наполнение БД данными
        final int AMOUNT_USER = 9;  // минимум 3 но не больше 80, четное 3, иначе тест не пройдет ввиду кол-ва дней в месяце
        final int AMOUNT_ITEM = 5;
        int size = 0;


        // запоминаем добавленных пользователей и айтемов
        List<Long> userIds = new ArrayList<>();
        List<ItemDto> userItems = new ArrayList<>();
        List<BookingDto> returnedBookings = new ArrayList<>();

        // добавляем в БД новых пользователей
        List<UserDto> dtoUsers = makeUsersDto("UserForTestBooking", "BookEmail@ya.ru", AMOUNT_USER);
        for (UserDto dtoUser : dtoUsers) {
            userIds.add(userService.create(dtoUser).getId());
        }

        // добавляем несколько Item для 2/3 пользователей
        for (Long userId : userIds) {
            size++;
            if (userIds.size() / 3 * 2 >= size) {
                List<ItemDto> itemDtoList = makeItems(
                        "айтем", "для теста на бронирование", userId, AMOUNT_ITEM);
                // добавлем по одной вещи каждого пользователя в БД
                for (ItemDto itemDto : itemDtoList) {
                    userItems.add(itemService.add(userId.toString(), itemDto));
                }

                // попытка добавить некорректные бронирования:
                try {
                    // 1) дата начала бронирования позже даты окончания
                    BookingCreatingDto bookingCreatingDto1 = makeNewBooking(userItems.getFirst().getId(),
                            "2024-11-", "2024-10-", size);
                    returnedBookings.add(bookingService.create(userId.toString(), bookingCreatingDto1));
                } catch (BadRequestException e) {
                    // do nothing
                    log.info("попытка добавить бронирование с датой начала позже окончания");
                }
                try {
                    // 2) бронирование осуществляет владелец вещи
                    BookingCreatingDto bookingCreatingDto2 = makeNewBooking(userItems.get(
                            (size * AMOUNT_ITEM) - 1).getId(), "2024-10-", "2024-10-", size);
                    returnedBookings.add(bookingService.create(userId.toString(), bookingCreatingDto2));
                } catch (BadRequestException e) {
                    // do nothing
                    log.info("попытка добавить бронирование владельцем вещи");
                }

                //когда все айтемы добавлены, добавляем бронирование под остающимися 1/3 пользователями
            } else {
                for (ItemDto userItem : userItems) {
                    BookingCreatingDto bookingCreatingDto = makeNewBooking(userItem.getId(),
                            "2024-10-", "2024-10-", size);
                    returnedBookings.add(bookingService.create(userId.toString(), bookingCreatingDto));

                }
            }
        }

        // получаем все бронирования
        TypedQuery<Booking> updQuery = em.createQuery("SELECT b FROM Booking AS b", Booking.class);
        List<Booking> bookings = updQuery.getResultList();

        // проверяем количество всех добавленных бронирований - должно быть 2/9 * на квадрат кол-ва пользователей * кол-ва items
        assertThat(bookings.size(), equalTo(AMOUNT_USER * AMOUNT_USER * AMOUNT_ITEM * 2 / 9));
        assertThat(bookings.size(), equalTo(returnedBookings.size()));

        // подтверждаем бронирование владельцем каждой вещи
        for (Long userId : userIds) {
            List<ItemBookTimeDto> items = itemService.getAllByOwner(userId.toString());
            if (!items.isEmpty()) {
                List<Long> itemId = items.stream().map(ItemBookTimeDto::getId).toList();
                for (Long l : itemId) {
                    for (BookingDto returnedBooking : returnedBookings) {
                        if (returnedBooking.getItem().getId().equals(l)) {
                            bookingService.changeStatus(userId.toString(),
                                    makeBookStatus(returnedBooking.getId(), true));
                            break;  // исключаем дублирование подтверждение по бронированию
                        }
                    }
                }
            }
        }

        // получаем подтвержденные бронирования
        TypedQuery<Booking> updQuery2 = em.createQuery(
                "SELECT b " +
                        "FROM Booking AS b " +
                        "WHERE b.status LIKE 'APPROVED'", Booking.class);
        List<Booking> bookings2 = updQuery2.getResultList();

        // проверяем количество всех подтвержденных бронирований - должно быть 2/27 * на квадрат кол-ва пользователей * кол-ва items
        assertThat(bookings2.size(), equalTo(AMOUNT_USER * AMOUNT_USER * AMOUNT_ITEM * 2 / 27));

    }


    private List<ItemDto> makeItems(String name, String description, long userId, int amountItem) {
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

    private BookingStatusDto makeBookStatus(Long bookingId, boolean status) {
        return BookingStatusDto.builder()
                .id(bookingId)
                .approve(status)
                .build();
    }

}