package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.BookingRepository;
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

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplTest {

    private final ItemRequestRepository repository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemResponseRepository responseRepository;
    private final BookingRepository bookingRepository;

    private final EntityManager em;
    private final UserServiceImpl userService;
    private final ItemServiceImpl itemService;
    private final ItemRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        responseRepository.deleteAll();
        repository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getOwnRequests() {

        // задаем параметры для теста, влияющие на наполнение БД данными
        final int AMOUNT_USER = 5;
        final int AMOUNT_ITEM = 4;

        // запоминаем добавленных пользователей и айтемов
        List<Long> userIds = new ArrayList<>();
        List<ItemDto> userItems = new ArrayList<>();
        List<Long> requestDtos = new ArrayList<>();

        // добавляем в БД новых пользователей
        List<UserDto> dtoUsers = makeUsersDto("UserForTestRequest", "BookEmail@ya.ru", AMOUNT_USER);
        for (UserDto dtoUser : dtoUsers) {
            userIds.add(userService.create(dtoUser).getId());
        }

        for (Long userId : userIds) {
            // Добавляем запросы в БД
            requestDtos.add(service.add(userId.toString(),
                    ItemRequestDto.builder()
                            .description("нужна вещица для userId=" + userId)
                            .build()).getId());
            requestDtos.add(service.add(userId.toString(),
                    ItemRequestDto.builder()
                            .description("нужна вторая вещь для userId=" + userId)
                            .build()).getId());
        }

        for (Long userId : userIds) {
            // добавляем Item без requestId
            List<ItemDto> itemDtoList = makeItems(
                    "айтем без запроса", "тест на ItemRequest", userId, AMOUNT_ITEM);
            for (ItemDto itemDto : itemDtoList) {
                userItems.add(itemService.add(userId.toString(), itemDto));
            }

            // добавляем Item с requestId
            List<ItemDto> itemDtoList2 = makeItemsWithReq(
                    "айтем с запросом", "для теста на ItemRequest", userId, requestDtos);
            for (ItemDto itemDto : itemDtoList2) {
                userItems.add(itemService.add(userId.toString(), itemDto));
            }

            TypedQuery<ItemRequest> updQuery1 = em.createQuery(
                    "SELECT ir " +
                            "FROM ItemRequest AS ir " +
                            "JOIN ir.requestOwner AS u " +
                            "WHERE u.id = :id", ItemRequest.class);
            List<ItemRequest> itemRequestsFromDB = updQuery1.setParameter("id", userId).getResultList();

            // проверка количества записей в БД
            assertThat(itemRequestsFromDB.size(), equalTo(2));

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
                    .name(name + "_запроса " + req + " владельца с id=" + userId)
                    .description(description + "_запроса " + req + " владельца с id=" + userId)
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