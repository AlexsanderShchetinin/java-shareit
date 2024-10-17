package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @MockBean
    UserServiceImpl userService;

    @Autowired
    ObjectMapper userMapper;

    @Autowired
    private MockMvc mvc;


    @Test
    void getAll() throws Exception {

        final int AMOUNT_USERS = 50;
        List<UserDto> users = makeUsers(AMOUNT_USERS);

        when(userService.getAll())
                .thenReturn(users);

        mvc.perform(get("/users")
                        .content(userMapper.writeValueAsString(users))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").exists())
                // Проверка, что в массиве есть хотя бы один объект с id, name и email в разных местах листа
                .andExpect(jsonPath("$[*].id").value(hasItem(users.get(AMOUNT_USERS / 2).getId().intValue())))
                .andExpect(jsonPath("$[*].email").value(hasItem(users.get(AMOUNT_USERS / 3).getEmail())))
                .andExpect(jsonPath("$[*].name").value(hasItem(users.get(AMOUNT_USERS / 4).getName())))
                .andExpect(jsonPath("$.[0].name", is("Юзер Мокитович_0")))
                .andExpect(jsonPath("$.[0].email", is("Mock0.345@ya.ru")));

    }

    @Test
    void getById() throws Exception {

        UserDto userDto = UserDto.builder()
                .id(999L)
                .name("Юзер Мокитович")
                .email("Mock.345@ya.ru")
                .build();

        when(userService.getById(999L))
                .thenReturn(userDto);

        mvc.perform(get("/users/999")
                        .content(userMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

    }

    @Test
    void create() throws Exception {
        List<UserDto> userDtos = makeUsers(10);

        for (UserDto userDto : userDtos) {
            when(userService.create(Mockito.any()))
                    .thenReturn(userDto);

            mvc.perform(post("/users")
                            .content(userMapper.writeValueAsString(userDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                    .andExpect(jsonPath("$.name", is(userDto.getName())))
                    .andExpect(jsonPath("$.email", is(userDto.getEmail())));
        }
    }

    @Test
    void update() throws Exception {

        List<UserDto> userDtos = makeUsers(10);

        for (UserDto userDto : userDtos) {
            when(userService.update(Mockito.anyLong(), Mockito.any()))
                    .thenReturn(userDto);

            mvc.perform(patch("/users/" + userDto.getId())
                            .content(userMapper.writeValueAsString(userDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                    .andExpect(jsonPath("$.name", is(userDto.getName())))
                    .andExpect(jsonPath("$.email", is(userDto.getEmail())));
        }

    }

    @Test
    void deleteUser() throws Exception {

        UserDto userDto = UserDto.builder()
                .id(999L)
                .name("Юзер Мокитович")
                .email("Mock.delete@ya.ru")
                .build();


        userService.delete(Mockito.anyLong());

        mvc.perform(delete("/users/999")
                        .content(userMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

    }

    private List<UserDto> makeUsers(int amount) {
        List<UserDto> users = new ArrayList<>();
        for (long i = 0; i < amount; i++) {
            users.add(UserDto.builder()
                    .id(i + 1000)
                    .name("Юзер Мокитович_" + i)
                    .email("Mock" + i + ".345@ya.ru")
                    .build());
        }
        return users;
    }
}