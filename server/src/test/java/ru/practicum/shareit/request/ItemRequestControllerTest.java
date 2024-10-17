package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @MockBean
    ItemRequestServiceImpl service;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private List<UserDto> userDtos = new ArrayList<>();
    private final int AMOUNT_USER = 20;
    private final int AMOUNT_REQ = 10;


    @BeforeEach
    void setUp() {
        userDtos = makeUsers(AMOUNT_USER);
    }

    @Test
    void addRequestItem() throws Exception {
        ItemRequestDto newRequestDto = makeNewRequestDto();

        for (UserDto userDto : userDtos) {
            when(service.add(Mockito.anyString(), Mockito.any()))
                    .thenReturn(newRequestDto);

            mvc.perform(post("/requests")
                            .content(mapper.writeValueAsString(newRequestDto))
                            .header("X-Sharer-User-Id", userDto.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(newRequestDto.getId()), Long.class))
                    .andExpect(jsonPath("$.description", is(newRequestDto.getDescription())));
        }
    }

    @Test
    void getOwnRequests() throws Exception {
        for (UserDto userDto : userDtos) {
            List<ItemRequestDto> fullRequestsDto = makeFullRequestsDto(99, 199, AMOUNT_REQ);
            when(service.getOwnRequests(Mockito.anyString()))
                    .thenReturn(fullRequestsDto);

            mvc.perform(get("/requests")
                            .header("X-Sharer-User-Id", userDto.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].id").value(hasItem(98 + AMOUNT_REQ)))
                    .andExpect(jsonPath("$[*].description").value(
                            hasItem("only request num=" + (AMOUNT_REQ - 1))));
        }
    }

    @Test
    void getAllRequests() throws Exception {
        for (UserDto userDto : userDtos) {
            List<ItemRequestDto> fullRequestsDto = makeFullRequestsDto(99, 199, AMOUNT_REQ);
            when(service.getAllRequests(Mockito.anyString()))
                    .thenReturn(fullRequestsDto);

            mvc.perform(get("/requests/all")
                            .header("X-Sharer-User-Id", userDto.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].id").value(hasItem(98 + AMOUNT_REQ)))
                    .andExpect(jsonPath("$[*].description").value(
                            hasItem("only request num=" + (AMOUNT_REQ - 1))));
        }
    }

    @Test
    void getRequestItem() throws Exception {
        for (UserDto userDto : userDtos) {
            ItemRequestDto requestDto = makeNewRequestDto();
            when(service.get(Mockito.anyString(), Mockito.anyLong()))
                    .thenReturn(requestDto);

            mvc.perform(get("/requests/" + 50)
                            .header("X-Sharer-User-Id", userDto.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                    .andExpect(jsonPath("$.description", is(requestDto.getDescription())));
        }
    }


    private List<UserDto> makeUsers(int amount) {
        List<UserDto> users = new ArrayList<>();
        for (long i = 0; i < amount; i++) {
            users.add(UserDto.builder()
                    .id(i + 1000L)
                    .name("Юзер Мокитович_" + i)
                    .email("Mock" + i + ".345@ya.ru")
                    .build());
        }
        return users;
    }

    private List<ItemRequestDto> makeFullRequestsDto(long itemId, long ownerId, long amount) {
        List<ItemRequestDto> requests = new ArrayList<>();
        for (long i = 0; i < amount; i++) {
            requests.add(ItemRequestDto.builder()
                    .id(99L + i)
                    .description("only request num=" + i)
                    .items(makeResponseDto(itemId, ownerId))
                    .created(LocalDateTime.of(2024, 10, 17, 16, 21))
                    .build()
            );
        }
        return requests;
    }

    private ItemRequestDto makeNewRequestDto() {
        return ItemRequestDto.builder()
                .id(99L)
                .description("only one the same request")
                .created(LocalDateTime.now())
                .build();
    }

    private List<ItemResponseDto> makeResponseDto(long itemId, long ownerId) {
        return List.of(ItemResponseDto.builder()
                .name("Response for itemId=" + itemId)
                .textResponse("sameText")
                .ownerId(ownerId)
                .itemId(itemId)
                .build());
    }
}