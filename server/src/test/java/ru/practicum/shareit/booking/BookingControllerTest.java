package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStatusDto;
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

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @MockBean
    BookingServiceImpl service;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private List<UserDto> userDtos = new ArrayList<>();

    @BeforeEach
    void setUp() {
        int amountUser = 20;
        userDtos = makeUsers(amountUser);
    }

    @Test
    void addBooking() throws Exception {
        BookingDto bookingDto = makeNewBooking();

        for (UserDto userDto : userDtos) {
            when(service.create(Mockito.anyString(), Mockito.any()))
                    .thenReturn(bookingDto);

            mvc.perform(post("/bookings")
                            .content(mapper.writeValueAsString(bookingDto))
                            .header("X-Sharer-User-Id", userDto.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                    .andExpect(jsonPath("$.item.id", is(bookingDto.getItem().getId().intValue())))
                    .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId().intValue())));
        }


    }

    @Test
    void changeStatus() throws Exception {
        BookingDto bookingDto = makeApproveBooking();

        BookingStatusDto bookingStatusDto = BookingStatusDto.builder()
                .id(bookingDto.getId())
                .approve(true)
                .build();

        for (UserDto userDto : userDtos) {
            when(service.changeStatus(Mockito.anyString(), Mockito.any()))
                    .thenReturn(bookingDto);

            mvc.perform(patch("/bookings/" + bookingDto.getId().intValue())
                            .content(mapper.writeValueAsString(bookingStatusDto))
                            .header("X-Sharer-User-Id", userDto.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                    .andExpect(jsonPath("$.status", is("APPROVED")));
        }

    }

    @Test
    void getById() throws Exception {

        BookingDto bookingDto = makeNewBooking();

        for (UserDto userDto : userDtos) {
            when(service.getById(Mockito.anyString(), Mockito.anyLong()))
                    .thenReturn(bookingDto);

            mvc.perform(get("/bookings/" + bookingDto.getId().intValue())
                            .header("X-Sharer-User-Id", userDto.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                    .andExpect(jsonPath("$.item.id", is(bookingDto.getItem().getId().intValue())))
                    .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId().intValue())))
                    .andExpect(jsonPath("$.status", is("WAITING")));
        }
    }

    @Test
    void getBookingsByBooker() throws Exception {
        List<BookingDto> newBookings = makeNewBookings(5);

        for (UserDto userDto : userDtos) {
            when(service.getBookingsByBooker(Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(newBookings);

            mvc.perform(get("/bookings")
                            .header("X-Sharer-User-Id", userDto.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].id").value(hasItem(newBookings.getFirst().getId().intValue())))
                    .andExpect(jsonPath("$[*].item.id").value(hasItem(newBookings.getLast().getItem().getId().intValue())))
                    .andExpect(jsonPath("$[*].booker.id").value(hasItem(newBookings.getFirst().getBooker().getId().intValue())))
                    .andExpect(jsonPath("$[*].status").value(hasItem("WAITING")));
        }
    }

    @Test
    void getBookingsByOwner() throws Exception {
        List<BookingDto> newBookings = makeNewBookings(5);

        for (UserDto userDto : userDtos) {
            when(service.getBookingsByOwner(Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(newBookings);

            mvc.perform(get("/bookings/owner")
                            .header("X-Sharer-User-Id", userDto.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].id").value(hasItem(newBookings.getFirst().getId().intValue())))
                    .andExpect(jsonPath("$[*].item.id").value(hasItem(newBookings.getLast().getItem().getId().intValue())))
                    .andExpect(jsonPath("$[*].booker.id").value(hasItem(newBookings.getFirst().getBooker().getId().intValue())))
                    .andExpect(jsonPath("$[*].status").value(hasItem("WAITING")));
        }

    }

    private List<BookingDto> makeNewBookings(long amount) {
        List<BookingDto> bookings = new ArrayList<>();
        for (long i = 0; i < amount; i++) {
            bookings.add(BookingDto.builder()
                    .id(2500L + i)
                    .booker(makeUsers(1).getFirst())
                    .item(makeAvailableItem())
                    .start(LocalDateTime.now())
                    .end(LocalDateTime.now().plusHours(1))
                    .status(BookingStatus.WAITING)
                    .build());
        }
        return bookings;
    }

    private BookingDto makeNewBooking() {
        return BookingDto.builder()
                .id(2500L)
                .booker(makeUsers(1).getFirst())
                .item(makeAvailableItem())
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(1))
                .status(BookingStatus.WAITING)
                .build();
    }

    private BookingDto makeApproveBooking() {
        return BookingDto.builder()
                .id(3500L)
                .booker(makeUsers(1).getFirst())
                .item(makeAvailableItem())
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(1))
                .status(BookingStatus.APPROVED)
                .build();
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