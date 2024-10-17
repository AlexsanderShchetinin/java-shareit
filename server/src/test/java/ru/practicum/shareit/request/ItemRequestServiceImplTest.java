package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.MyNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.resp.ItemResponseRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplTest {

    private static final Logger log = LoggerFactory.getLogger(ItemRequestServiceImplTest.class);
    private final ItemRequestRepository repository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemResponseRepository responseRepository;
    private final BookingRepository bookingRepository;

    private final EntityManager em;
    private final UserServiceImpl userService;
    private final ItemServiceImpl itemService;
    private final ItemRequestServiceImpl service;
    // задаем параметры для теста, влияющие на наполнение БД данными
    private final static int AMOUNT_USER = 5;
    private final static int AMOUNT_ITEM = 4;
    private final static int AMOUNT_ITEM_WITH_REQ = 2;  // в setUp() равно вызовам service.add()
    // переменные для добавленных пользователей и айтемов
    private final List<Long> userIds = new ArrayList<>();
    private final List<ItemDto> userItems = new ArrayList<>();
    private final List<Long> requestIds = new ArrayList<>();
    private final List<ItemRequestDto> requestsDto = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        responseRepository.deleteAll();
        repository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        // добавляем в БД новых пользователей
        List<UserDto> dtoUsers = makeUsersDto("UserForTestRequest", "BookEmail@ya.ru", AMOUNT_USER);
        for (UserDto dtoUser : dtoUsers) {
            userIds.add(userService.create(dtoUser).getId());
        }

        for (Long userId : userIds) {
            // Добавляем запросы в БД
            requestsDto.add((service.add(userId.toString(),
                            ItemRequestDto.builder()
                                    .description("нужна вещица для userId=" + userId)
                                    .build()
                    ))
            );
            requestsDto.add(service.add(userId.toString(),
                            ItemRequestDto.builder()
                                    .description("нужна вторая вещь для userId=" + userId)
                                    .build()
                    )
            );
            // запоминаем id запросов
            requestIds.addAll(requestsDto.stream()
                    .map(ItemRequestDto::getId)
                    .toList());

            // добавляем Item без requestId
            List<ItemDto> itemDtoList = makeItems(
                    "айтем без запроса", "тест на ItemRequest", userId, AMOUNT_ITEM);
            for (ItemDto itemDto : itemDtoList) {
                userItems.add(itemService.add(userId.toString(), itemDto));
            }

            // добавляем Item с requestId
            List<ItemDto> itemDtoList2 = makeItemsWithReq(
                    "айтем с запросом", "для теста на ItemRequest=", userId, requestIds);
            for (ItemDto itemDto : itemDtoList2) {
                userItems.add(itemService.add(userId.toString(), itemDto));
            }
            // очищаем id запросов для след юзера
            requestIds.clear();
        }
    }

    @Test
    void getOwnRequests() {
        for (Long userId : userIds) {
            TypedQuery<ItemRequest> updQuery1 = em.createQuery(
                    "SELECT ir " +
                            "FROM ItemRequest AS ir " +
                            "JOIN ir.requestOwner AS u " +
                            "WHERE u.id = :id ", ItemRequest.class);
            List<ItemRequest> resp = updQuery1.setParameter("id", userId).getResultList();

            List<ItemRequestDto> requestsFromService = service.getOwnRequests(userId.toString());

            // проверка записей в БД и возврата из сервиса
            assertThat(requestsFromService.size(), equalTo(resp.size()));
            assertThat(resp.size(), equalTo(AMOUNT_ITEM_WITH_REQ));
        }
    }

    @Test
    void getReqById() {
        for (ItemRequestDto requestDto : requestsDto) {
            TypedQuery<ItemRequest> updQuery1 = em.createQuery(
                    "SELECT ir " +
                            "FROM ItemRequest AS ir " +
                            "WHERE ir.id = :id ", ItemRequest.class);
            ItemRequest resp = updQuery1.setParameter("id", requestDto.getId()).getSingleResult();

            try {
                ItemRequestDto itemRequestDto = service.get("9999", requestDto.getId());
            } catch (MyNotFoundException e) {
                log.info("отлов попытки получения request некорректным пользователем");
            }

            ItemRequestDto itemRequestDto = service.get(userIds.getFirst().toString(), requestDto.getId());

            assertThat(resp.getDescription(), equalTo(requestDto.getDescription()));
            assertThat(resp.getDescription(), equalTo(itemRequestDto.getDescription()));
            assertThat(resp.getId(), notNullValue());
            assertThat(itemRequestDto.getId(), equalTo(resp.getId()));
        }
    }

    @Test
    void getAllRequests() {  // должен возвращать все запросы, исключая те что были созданы пользователем
        for (Long userId : userIds) {
            TypedQuery<ItemRequest> updQuery1 = em.createQuery(
                    "SELECT ir " +
                            "FROM ItemRequest AS ir " +
                            "JOIN ir.requestOwner AS u " +
                            "WHERE u.id != :id ", ItemRequest.class);
            List<ItemRequest> resp = updQuery1.setParameter("id", userId).getResultList();

            List<ItemRequestDto> requestsFromService = service.getAllRequests(userId.toString());

            // проверка записей в БД и возврата из сервиса
            assertThat(requestsFromService.size(), equalTo(resp.size()));
            assertThat(resp.size(), equalTo(AMOUNT_ITEM_WITH_REQ * (AMOUNT_USER - 1)));
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

    private List<ItemDto> makeItemsWithReq(String name, String description, long userId, List<Long> requestIds) {
        List<ItemDto> items = new ArrayList<>();
        for (long req : requestIds) {
            items.add(ItemDto.builder()
                    .name(name + req + " владельца с id=" + userId)
                    .description(description + req + " владельца с id=" + userId)
                    .available(true)
                    .requestId(req)
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