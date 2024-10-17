package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookTimeDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @MockBean
    ItemServiceImpl itemService;

    @Autowired
    ObjectMapper itemMapper;

    @Autowired
    private MockMvc mvc;

    private List<UserDto> userDtos = new ArrayList<>();
    private final static int AMOUNT_USER = 20;
    private final static int AMOUNT_ITEM = 10;

    @BeforeEach
    void setUp() {
        userDtos = makeUsers(AMOUNT_USER);
    }

    @Test
    void getAllByOwner() throws Exception {
        List<ItemBookTimeDto> itemsDto = makeAvailableBookTimeItems(AMOUNT_ITEM);

        for (UserDto userDto : userDtos) {
            when(itemService.getAllByOwner(Mockito.anyString()))
                    .thenReturn(itemsDto);

            mvc.perform(get("/items")
                            .content(itemMapper.writeValueAsString(itemsDto))
                            .header("X-Sharer-User-Id", userDto.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].id").
                            value(hasItem(itemsDto.get(AMOUNT_ITEM / 2).getId().intValue())))
                    .andExpect(jsonPath("$[*].name").value(hasItem(itemsDto.get(AMOUNT_ITEM / 2).getName())))
                    .andExpect(jsonPath("$[*].description")
                            .value(hasItem(itemsDto.get(AMOUNT_ITEM / 2).getDescription())))
                    .andExpect(jsonPath("$.[0].available", is(itemsDto.getFirst().getAvailable())));
        }
    }

    @Test
    void getById() throws Exception {

        ItemBookTimeDto itemDto = makeAvailableBookTimeItem();

        for (UserDto userDto : userDtos) {
            when(itemService.getById(Mockito.anyString(), Mockito.anyLong()))
                    .thenReturn(itemDto);

            mvc.perform(get("/items/" + itemDto.getId())
                            .content(itemMapper.writeValueAsString(itemDto))
                            .header("X-Sharer-User-Id", userDto.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                    .andExpect(jsonPath("$.name", is(itemDto.getName())))
                    .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                    .andExpect(jsonPath("$.comments.[0].id",
                            is(itemDto.getComments().getFirst().getId().intValue())))
                    .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
        }
    }

    @Test
    void getSelection() throws Exception {
        List<ItemDto> itemsDto = makeAvailableItems(AMOUNT_ITEM);

        for (UserDto userDto : userDtos) {
            when(itemService.getSelection(Mockito.anyString()))
                    .thenReturn(itemsDto);

            mvc.perform(get("/items/search?text=" + "jUst")
                            .content(itemMapper.writeValueAsString(itemsDto))
                            .header("X-Sharer-User-Id", userDto.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].id").
                            value(hasItem(itemsDto.get(AMOUNT_ITEM / 2).getId().intValue())))
                    .andExpect(jsonPath("$[*].name").value(hasItem(itemsDto.get(AMOUNT_ITEM / 2).getName())))
                    .andExpect(jsonPath("$[*].description")
                            .value(hasItem(itemsDto.get(AMOUNT_ITEM / 2).getDescription())))
                    .andExpect(jsonPath("$.[0].available", is(itemsDto.getFirst().getAvailable())));
        }
    }

    @Test
    void add() throws Exception {
        ItemDto itemDto = makeAvailableItem();

        for (UserDto userDto : userDtos) {
            when(itemService.add(Mockito.anyString(), Mockito.any()))
                    .thenReturn(itemDto);

            mvc.perform(post("/items")
                            .content(itemMapper.writeValueAsString(itemDto))
                            .header("X-Sharer-User-Id", userDto.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                    .andExpect(jsonPath("$.name", is(itemDto.getName())))
                    .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                    .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
        }
    }

    @Test
    void addComment() throws Exception {
        ItemDto itemDto = makeAvailableItem();
        CommentDto commentDto = makeComments(itemDto, 1).getFirst();

        for (UserDto userDto : userDtos) {
            when(itemService.addComment(Mockito.any()))
                    .thenReturn(commentDto);

            mvc.perform(post("/items/" + itemDto.getId().intValue() + "/comment")
                            .content(itemMapper.writeValueAsString(commentDto))
                            .header("X-Sharer-User-Id", userDto.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                    .andExpect(jsonPath("$.text", is(commentDto.getText())))
                    .andExpect(jsonPath("$.item.id", is(commentDto.getItem().getId().intValue())))
                    .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())));
        }
    }

    @Test
    void update() throws Exception {
        ItemDto itemDto = makeAvailableItem();

        for (UserDto userDto : userDtos) {
            when(itemService.update(Mockito.anyString(), Mockito.any()))
                    .thenReturn(itemDto);

            mvc.perform(patch("/items/" + userDto.getId())
                            .content(itemMapper.writeValueAsString(itemDto))
                            .header("X-Sharer-User-Id", userDto.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                    .andExpect(jsonPath("$.name", is(itemDto.getName())))
                    .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                    .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
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

    private List<ItemDto> makeAvailableItems(int amount) {
        List<ItemDto> items = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            items.add(ItemDto.builder()
                    .id(i + 2000L)
                    .available(true)
                    .name("just Item_" + 900 + i)
                    .description("доступная, номер_" + 1 + i)
                    .build());
        }
        return items;
    }

    private ItemDto makeAvailableItem() {
        return ItemDto.builder()
                .id(999L)
                .available(true)
                .name("Single create Item")
                .description("доступная - description the same!")
                .build();
    }

    private List<ItemBookTimeDto> makeAvailableBookTimeItems(int amount) {
        List<ItemBookTimeDto> items = new ArrayList<>();
        for (long i = 0; i < amount; i++) {
            items.add(ItemBookTimeDto.builder()
                    .id(5000L + i)
                    .available(true)
                    .name("ItemBookTimeDto_" + i)
                    .description("доступная - description_" + i)
                    .comments(makeComments(makeAvailableItem(), 50))
                    .build());
        }
        return items;
    }

    private ItemBookTimeDto makeAvailableBookTimeItem() {
        return ItemBookTimeDto.builder()
                .id(999L)
                .available(true)
                .name("Single create Item")
                .description("доступная - description the same!")
                .comments(makeComments(makeAvailableItem(), 15))
                .build();

    }

    private List<CommentDto> makeComments(ItemDto itemDto, int amount) {
        List<CommentDto> comments = new ArrayList<>();
        for (long i = 0; i < amount; i++) {
            comments.add(CommentDto.builder()
                    .id(i + 3000L)
                    .item(itemDto)
                    .text("text comment num_" + i)
                    .authorName("Random author")
                    .created(LocalDateTime.now())
                    .build());
        }
        return comments;
    }


}