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
import ru.practicum.shareit.exception.InterruptionRuleException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.ItemBookTimeDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.resp.ItemResponseRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.Duration;
import java.time.LocalDateTime;
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
    // задаем параметры для теста, влияющие на наполнение БД данными
    final int AMOUNT_USER = 9;  // минимум 3 но не больше 80, четное 3, иначе тест не пройдет ввиду кол-ва дней в месяце
    final int AMOUNT_ITEM = 5;
    int size;
    final long wait = 3;
    // запоминаем добавленных пользователей и айтемов
    private List<Long> userIds;
    private List<ItemDto> userItems;
    private List<BookingDto> returnedBookings;

    @BeforeEach
    void setUp() {
        // перед тестом очищаем БД
        repository.deleteAll();
        itemRespRepository.deleteAll();
        itemReqRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
        // обнуляем параметры теста
        size = 0;
        userIds = new ArrayList<>();
        userItems = new ArrayList<>();
        returnedBookings = new ArrayList<>();
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
                    BookingCreatingDto bookingCreatingDto1 = makeNewBookingHours(userItems.getFirst().getId(),
                            LocalDateTime.now(), -size);
                    returnedBookings.add(bookingService.create(userId.toString(), bookingCreatingDto1));
                } catch (BadRequestException e) {
                    // do nothing
                    log.info("попытка добавить бронирование с датой начала позже окончания");
                }
                try {
                    // 2) бронирование осуществляет владелец вещи
                    BookingCreatingDto bookingCreatingDto2 = makeNewBookingHours(userItems.get(
                            (size * AMOUNT_ITEM) - 1).getId(), LocalDateTime.now(), size);
                    returnedBookings.add(bookingService.create(userId.toString(), bookingCreatingDto2));
                } catch (BadRequestException e) {
                    // do nothing
                    log.info("попытка добавить бронирование владельцем вещи");
                }
                //когда все айтемы добавлены, добавляем бронирование под остающимися 1/3 пользователями
            } else {
                for (ItemDto userItem : userItems) {
                    // текущие, без подтверждения
                    returnedBookings.add(bookingService.create(userId.toString(),
                            makeNewBookingHours(userItem.getId(), LocalDateTime.now(), size)));
                }
            }
        }
    }

    @Test
    void getBookings() {
        // получаем все бронирования
        TypedQuery<Booking> updQuery = em.createQuery("SELECT b FROM Booking AS b", Booking.class);
        List<Booking> bookings = updQuery.getResultList();

        // проверяем количество всех добавленных бронирований
        assertThat(bookings.size(), equalTo(AMOUNT_USER * AMOUNT_USER * AMOUNT_ITEM * 2 / 9));
        assertThat(bookings.size(), equalTo(returnedBookings.size()));

        // подтверждаем бронирование владельцем каждой одной вещи, остальные бронирования отклоняем
        changeStatusOwner();

        // получаем подтвержденные бронирования
        TypedQuery<Booking> updQuery2 = em.createQuery(
                "SELECT b " +
                        "FROM Booking AS b " +
                        "WHERE b.status LIKE 'APPROVED'", Booking.class);
        List<Booking> bookings2 = updQuery2.getResultList();

        // проверяем количество всех подтвержденных бронирований - должно быть 2/27 * на квадрат кол-ва пользователей * кол-ва items
        assertThat(bookings2.size(), equalTo(AMOUNT_USER * AMOUNT_USER * AMOUNT_ITEM * 2 / 27));
    }

    @Test
    void getById() {
        // получаем бронирование владельцем вещи
        for (Long userId : userIds) {
            TypedQuery<Booking> updQuery = em.createQuery("SELECT b " +
                    "FROM Booking AS b " +
                    "JOIN b.bookingItem AS i " +
                    "JOIN i.owner AS u  " +
                    "WHERE u.id = :id", Booking.class);
            List<Booking> bookings = updQuery.setParameter("id", userId).getResultList();

            if (!bookings.isEmpty()) {
                for (Booking booking : bookings) {
                    BookingDto returnBooking = bookingService.getById(userId.toString(), booking.getId());

                    assertThat(booking.getBookingAuthor().getId(), equalTo(returnBooking.getBooker().getId()));
                    assertThat(booking.getBookingItem().getId(), equalTo(returnBooking.getItem().getId()));
                    assertThat(booking.getStatus(), equalTo(returnBooking.getStatus()));
                    assertThat(booking.getStart(), equalTo(returnBooking.getStart()));
                    assertThat(booking.getEnd(), equalTo(returnBooking.getEnd()));
                }
            }
        }

        // получаем бронирование автором бронирования
        for (Long userId : userIds) {
            TypedQuery<Booking> updQuery = em.createQuery("SELECT b " +
                    "FROM Booking AS b " +
                    "JOIN b.bookingAuthor AS u " +
                    "WHERE u.id = :id", Booking.class);
            List<Booking> bookings = updQuery.setParameter("id", userId).getResultList();
            if (!bookings.isEmpty()) {
                for (Booking booking : bookings) {
                    BookingDto returnBooking = bookingService.getById(userId.toString(), booking.getId());

                    assertThat(booking.getBookingAuthor().getId(), equalTo(returnBooking.getBooker().getId()));
                    assertThat(booking.getBookingItem().getId(), equalTo(returnBooking.getItem().getId()));
                    assertThat(booking.getStatus(), equalTo(returnBooking.getStatus()));
                    assertThat(booking.getStart(), equalTo(returnBooking.getStart()));
                    assertThat(booking.getEnd(), equalTo(returnBooking.getEnd()));
                }
            }
        }

        // попытка получить бронирование сторонним пользователем
        try {
            for (Long userId : userIds) {
                TypedQuery<Booking> updQuery = em.createQuery("SELECT b " +
                        "FROM Booking AS b " +
                        "JOIN b.bookingItem AS i " +
                        "JOIN i.owner AS u  " +
                        "WHERE u.id != :id " +
                        "AND b.bookingAuthor.id != :id", Booking.class);
                List<Booking> bookings = updQuery.setParameter("id", userId).getResultList();
                if (!bookings.isEmpty()) {
                    for (Booking booking : bookings) {
                        bookingService.getById(userId.toString(), booking.getId());
                    }
                }
            }
        } catch (InterruptionRuleException e) {
            // DO NOTHING
        }
    }

    // тест получения бронирований по владельцу вещей
    @Test
    void getBookingsByOwner() throws InterruptedException {
        // в setUp() созданы бронирования ожидающие подтверждения - все со статусом Waiting
        // проверим их количество при получении из БД по каждому владельцу:
        for (Long userId : userIds) {
            TypedQuery<Booking> updQuery = em.createQuery("SELECT b " +
                    "FROM Booking AS b " +
                    "JOIN b.bookingItem AS i " +
                    "WHERE b.status LIKE :status " +
                    "AND (i.owner.id = :ownerId) " +
                    "ORDER BY b.start DESC", Booking.class);
            List<Booking> resp = updQuery.setParameter("status", BookingStatus.WAITING)
                    .setParameter("ownerId", userId).getResultList();
            if (!resp.isEmpty()) {
                List<BookingDto> bookings = bookingService.getBookingsByOwner(userId.toString(), "WAItInG");

                assertThat(bookings.size(), equalTo(resp.size()));
                assertThat(bookings.size(), equalTo(AMOUNT_USER * AMOUNT_ITEM / 3));
            }
        }

        // изменим статусы бронирований
        changeStatusOwner();

        // заново проверим работу метода с параметрами CURRENT, REJECTED и ALL (по умолчанию)
        // должна быть треть от всех подтверждены + две трети отклонены = ALL по каждому владельцу items
        for (Long userId : userIds) {
            TypedQuery<Booking> updQuery = em.createQuery("SELECT b " +
                    "FROM Booking AS b " +
                    "JOIN b.bookingItem AS i " +
                    "WHERE i.owner.id = :ownerId", Booking.class);
            List<Booking> resp = updQuery.setParameter("ownerId", userId).getResultList();
            if (!resp.isEmpty()) {
                List<BookingDto> rejects = bookingService.getBookingsByOwner(userId.toString(), "reJecteD");
                List<BookingDto> approves = bookingService.getBookingsByOwner(userId.toString(), "CURrENt");
                List<BookingDto> all = bookingService.getBookingsByOwner(userId.toString(), "alL");

                assertThat(rejects.size(), equalTo(AMOUNT_USER * AMOUNT_ITEM * 2 / 9));
                assertThat(approves.size(), equalTo(AMOUNT_USER * AMOUNT_ITEM / 9));
                assertThat(all.size(), equalTo(rejects.size() + approves.size()));
            }
        }

        // проверяем прошедшие и будущие бронирования
        // добавляем еще бронирования для существующих Items = users*items=45 штук (если не изменять переменные)
        for (Long userId : userIds) {
            TypedQuery<Item> updQuery = em.createQuery("SELECT i " +
                    "FROM Item AS i " +
                    "WHERE i.owner.id != :ownerId", Item.class);
            List<Item> resp = updQuery.setParameter("ownerId", userId).getResultList();
            for (Item item : resp) {
                // будущее
                bookingService.create(userId.toString(),
                        makeNewBookingHours(item.getId(), LocalDateTime.now().plusHours(1L), wait));
                // прошедшее
                bookingService.create(userId.toString(),
                        makeNewBookingSecond(item.getId(), LocalDateTime.now(), wait));
            }
        }
        //ожидаем пока бронирование завершится
        long endTime = System.currentTimeMillis() + (1000 * (wait + 1)); // wait секунд
        while (System.currentTimeMillis() <= endTime) {
            // Ничего не делать, просто ждать
        }

        // проверяем
        for (Long userId : userIds) {
            TypedQuery<Booking> updQuery = em.createQuery("SELECT b " +
                    "FROM Booking AS b " +
                    "JOIN b.bookingItem AS i " +
                    "WHERE i.owner.id = :ownerId", Booking.class);
            List<Booking> resp = updQuery.setParameter("ownerId", userId).getResultList();
            if (!resp.isEmpty()) {
                List<BookingDto> pasts = bookingService.getBookingsByOwner(userId.toString(), "past");
                List<BookingDto> futures = bookingService.getBookingsByOwner(userId.toString(), "future");

                // общее
                assertThat(pasts.size(), equalTo((AMOUNT_USER * AMOUNT_ITEM) - AMOUNT_ITEM));
                assertThat(futures.size(), equalTo((AMOUNT_USER * AMOUNT_ITEM) - AMOUNT_ITEM));
            }
        }
    }

    // тест получения бронирований по создателю бронирований
    @Test
    void getBookingsByBooker() {
        // в setUp() созданы бронирования ожидающие подтверждения - все со статусом Waiting
        // проверим их количество при получении из БД по каждому автору:
        for (Long userId : userIds) {
            TypedQuery<Booking> updQuery = em.createQuery("SELECT b " +
                    "FROM Booking AS b " +
                    "JOIN b.bookingAuthor AS u " +
                    "WHERE b.status LIKE :status " +
                    "AND (u.id = :userId)", Booking.class);
            List<Booking> resp = updQuery.setParameter("status", BookingStatus.WAITING)
                    .setParameter("userId", userId).getResultList();
            if (!resp.isEmpty()) {
                List<BookingDto> bookings = bookingService.getBookingsByBooker(userId.toString(), "WAItInG");

                assertThat(bookings.size(), equalTo(resp.size()));
                assertThat(bookings.size(), equalTo(AMOUNT_USER * AMOUNT_ITEM * 2 / 3));
            }
        }

        // изменим статусы бронирований
        changeStatusBooker();

        // заново проверим работу метода с параметрами CURRENT, REJECTED и ALL (по умолчанию)
        // должна быть треть от всех подтверждены + две трети отклонены = ALL по каждому владельцу items
        for (Long userId : userIds) {
            TypedQuery<Booking> updQuery = em.createQuery("SELECT b " +
                    "FROM Booking AS b " +
                    "JOIN b.bookingAuthor AS u " +
                    "WHERE u.id = :userId", Booking.class);
            List<Booking> resp = updQuery.setParameter("userId", userId).getResultList();
            if (!resp.isEmpty()) {
                List<BookingDto> rejects = bookingService.getBookingsByBooker(userId.toString(), "reJecteD");
                List<BookingDto> approves = bookingService.getBookingsByBooker(userId.toString(), "CURrENt");
                List<BookingDto> all = bookingService.getBookingsByBooker(userId.toString(), "alL");

                //assertThat(rejects.size(), equalTo(AMOUNT_USER * AMOUNT_ITEM * 2 / 3));
                //assertThat(approves.size(), equalTo(AMOUNT_USER * AMOUNT_ITEM * 2 / 3));
                assertThat(all.size(), equalTo(rejects.size() + approves.size()));
            }
        }

        // проверяем прошедшие и будущие бронирования
        // добавляем еще бронирования для существующих Items = users*items=30 штук (если не изменять переменные)
        for (Long userId : userIds) {
            TypedQuery<Item> updQuery = em.createQuery("SELECT i " +
                    "FROM Item AS i " +
                    "WHERE i.owner.id != :ownerId", Item.class);
            List<Item> resp = updQuery.setParameter("ownerId", userId).getResultList();
            for (Item item : resp) {
                // будущее
                bookingService.create(userId.toString(),
                        makeNewBookingHours(item.getId(), LocalDateTime.now().plusHours(1L), wait));
                // прошедшее
                bookingService.create(userId.toString(),
                        makeNewBookingSecond(item.getId(), LocalDateTime.now(), wait));
            }
        }
        //ожидаем пока бронирование завершится
        long endTime = System.currentTimeMillis() + (1000 * (wait + 1)); // wait секунд
        while (System.currentTimeMillis() <= endTime) {
            // Ничего не делать, просто ждать
        }

        // проверяем
        for (Long userId : userIds) {
            TypedQuery<Booking> updQuery = em.createQuery("SELECT b " +
                    "FROM Booking AS b " +
                    "JOIN b.bookingAuthor AS u " +
                    "WHERE u.id = :userId", Booking.class);
            List<Booking> resp = updQuery.setParameter("userId", userId).getResultList();
            if (!resp.isEmpty()) {
                bookingService.getBookingsByBooker(userId.toString(), "past");
                bookingService.getBookingsByBooker(userId.toString(), "future");
            }
        }
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

    // создание бронирования продолжительностью hour часов
    private BookingCreatingDto makeNewBookingHours(Long itemId, LocalDateTime start, long hour) {
        Duration duration = Duration.ofHours(hour);
        return BookingCreatingDto.builder()
                .itemId(itemId)
                .start(start)
                .end(start.plus(duration))
                .build();
    }

    // создание бронирования продолжительностью в секунды
    private BookingCreatingDto makeNewBookingSecond(Long itemId, LocalDateTime start, long second) {
        Duration duration = Duration.ofSeconds(second);
        return BookingCreatingDto.builder()
                .itemId(itemId)
                .start(start)
                .end(start.plus(duration))
                .build();
    }

    private BookingStatusDto makeBookStatus(Long bookingId, boolean status) {
        return BookingStatusDto.builder()
                .id(bookingId)
                .approve(status)
                .build();
    }

    private void changeStatusOwner() {
        for (Long userId : userIds) {
            int count = 1;
            List<ItemBookTimeDto> items = itemService.getAllByOwner(userId.toString());
            if (!items.isEmpty()) {
                List<Long> itemId = items.stream().map(ItemBookTimeDto::getId).toList();
                for (Long l : itemId) {
                    for (BookingDto returnedBooking : returnedBookings) {
                        if (returnedBooking.getItem().getId().equals(l)) {
                            if (count == AMOUNT_USER / 3) {
                                bookingService.changeStatus(userId.toString(),
                                        makeBookStatus(returnedBooking.getId(), true));
                                count = 1;
                            } else {
                                count++;
                                bookingService.changeStatus(userId.toString(),
                                        makeBookStatus(returnedBooking.getId(), false));
                            }
                        }
                    }
                }
            }
        }
    }

    private void changeStatusBooker() {
        for (Long userId : userIds) {
            int count = 1;
            TypedQuery<Booking> updQuery = em.createQuery("SELECT b " +
                    "FROM Booking AS b " +
                    "JOIN b.bookingAuthor AS u " +
                    "JOIN b.bookingItem AS i " +
                    "WHERE i.owner.id = :userId", Booking.class);
            List<Booking> resp = updQuery.setParameter("userId", userId).getResultList();
            if (!resp.isEmpty()) {
                for (Booking booking : resp) {
                    if (count == AMOUNT_USER / 3) {
                        bookingService.changeStatus(userId.toString(),
                                makeBookStatus(booking.getId(), true));
                        count = 1;
                    } else {
                        count++;
                        bookingService.changeStatus(userId.toString(),
                                makeBookStatus(booking.getId(), false));
                    }
                }
            }
        }
    }
}