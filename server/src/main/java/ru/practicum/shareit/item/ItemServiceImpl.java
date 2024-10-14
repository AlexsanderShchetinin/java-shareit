package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.InterruptionRuleException;
import ru.practicum.shareit.exception.MyNotFoundException;
import ru.practicum.shareit.item.dto.CommentCreationDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookTimeDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemListMapperImpl;
import ru.practicum.shareit.item.mapper.ItemMapperImpl;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.resp.ItemResponse;
import ru.practicum.shareit.request.resp.ItemResponseRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemResponseRepository responseRepository;

    private final ItemMapperImpl itemMapper;
    private final ItemListMapperImpl itemListMapper;


    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ItemDto add(String ownerStr, ItemDto itemDto) {
        long ownerId = Long.parseLong(ownerStr);
        User owner = userRepository.findById(ownerId)
                .orElseThrow(
                        () -> new MyNotFoundException("Владелец с id=" + ownerId + " не зарегистрирован в приложении"));
        Item item = itemMapper.toModel(itemDto);

        // сохранение ответа в БД на запрос request, при наличии запроса в ItemDto
        if (itemDto.getRequestId() == null) {
            item.setRequest(null);
        } else {
            ItemResponse response = ItemResponse.builder()
                    .request(item.getRequest())
                    .item(item)
                    .textResponse("here will be response comment")
                    .build();
            responseRepository.save(response);
        }

        item.setOwner(owner);
        Item itemFromRep = itemRepository.save(item);

        return itemMapper.toDto(itemFromRep);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ItemDto update(String ownerStr, ItemDto itemDto) {
        long ownerId = Long.parseLong(ownerStr);
        User owner = userRepository.findById(ownerId)
                .orElseThrow(
                        () -> new MyNotFoundException("Владелец с id=" + ownerId + " не зарегистрирован в приложении"));
        if (itemDto.getId() == null) {
            throw new MyNotFoundException("Не передан идентификатор Вещи для обновления");
        }
        Item returnedItem = itemRepository.findById(itemDto.getId())
                .orElseThrow(() -> new MyNotFoundException("Вещи с id=" + itemDto.getId() + " не существует."));
        if (!returnedItem.getOwner().equals(owner)) {
            throw new InterruptionRuleException("Редактировать Вещь может только её владелец");
        }

        // Если при обновлении передаются не все поля, то отсутствующие поля берем из репозитория
        if (itemDto.getName() == null || itemDto.getName().isEmpty()) {
            itemDto.setName(returnedItem.getName());
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isEmpty()) {
            itemDto.setDescription(returnedItem.getDescription());
        }
        if (itemDto.getAvailable() == null) {
            itemDto.setAvailable(returnedItem.isAvailable());
        }

        Item item = itemMapper.toModel(itemDto);
        item.setOwner(owner);
        if (item.getRequest().getId() == 0) {
            item.setRequest(null);
        }
        Item updatedItem = itemRepository.save(item);
        return itemMapper.toDto(updatedItem);
    }

    @Override
    public ItemBookTimeDto getById(String owner, long itemId) {
        long ownerId = Long.parseLong(owner);
        // проверка на наличие владельца и вещи в БД
        userRepository.findById(ownerId)
                .orElseThrow(
                        () -> new MyNotFoundException("Владелец с id=" + ownerId + " не зарегистрирован в приложении"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new MyNotFoundException("Вещи с id=" + itemId + " не существует."));

        ItemBookTimeDto itemDto = itemMapper.toBookingTimeDto(item);
        // все бронирования для одной вещи
        List<Booking> bookings = bookingRepository.findAllByBookingItemId(itemId);
        // все комментарии к вещи
        List<CommentDto> allItemComments = itemListMapper.toListCommentDto(
                commentRepository.findAllByItemId(itemId));
        // добавляем даты бронирования и сомментарии через отдельный метод
        setDatesAndComments(itemDto, bookings, allItemComments);
        return itemDto;
    }

    private void setDatesAndComments(
            ItemBookTimeDto item, List<Booking> bookings, List<CommentDto> allComments) {
        // устанавливаем дефолтовые даты
        LocalDateTime dateLastBooking = LocalDateTime.parse("2000-01-01T00:00:01",
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime dateNextBooking = LocalDateTime.parse("3000-01-01T00:00:01",
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        // проверяем каждое бронирование и заполняем даты
        for (Booking booking : bookings) {
            if (booking.getEnd().isBefore(LocalDateTime.now().minusHours(1))
                    && booking.getEnd().isAfter(dateLastBooking)) {
                dateLastBooking = booking.getEnd();
            }
            if (booking.getStart().isAfter(LocalDateTime.now()) && booking.getStart().isBefore(dateNextBooking)) {
                dateNextBooking = booking.getStart();
            }
        }
        // если даты были заполнены то они не равны дефолтовым, значит заполняем их в dto
        if (!dateLastBooking.equals(LocalDateTime.parse("2000-01-01T00:00:01",
                DateTimeFormatter.ISO_LOCAL_DATE_TIME))) {
            item.setLastBooking(dateLastBooking);
        }
        if (!dateNextBooking.equals(LocalDateTime.parse("3000-01-01T00:00:01",
                DateTimeFormatter.ISO_LOCAL_DATE_TIME))) {
            item.setNextBooking(dateNextBooking);
        }
        // проверяем каждый комментарий, относится ли он к вещи, если да, то заполняем лист comments
        List<CommentDto> comments = new ArrayList<>();
        for (CommentDto comment : allComments) {
            if (item.getId().equals(comment.getItem().getId())) {
                comments.add(comment);
            }
        }
        item.setComments(comments);
    }

    @Override
    public List<ItemBookTimeDto> getAllByOwner(String owner) {
        Long ownerId = Long.parseLong(owner);
        // все вещи владельца
        List<Item> items = itemRepository.findAllByOwnerId(ownerId);
        // все бронирования вещей с фильтрацией по владельцу (у одного владельца может быть несколько вещей)
        List<Booking> bookings = bookingRepository.findAllByBookingItemOwnerIdOrderByStart(ownerId);
        // находим все комментарии к вещам одного владельца
        List<CommentDto> allOwnerComments = itemListMapper.toListCommentDto(
                commentRepository.findAllByItemOwnerId(ownerId));
        List<ItemBookTimeDto> returnedItems = itemListMapper.toListBookingTimeDto(items);

        // для каждой вещи добавляем даты последнего, предстоящего бронирования и все комментарии
        for (ItemBookTimeDto item : returnedItems) {
            List<Booking> bookingsForItem = bookings.stream()
                    .filter(booking -> booking.getBookingItem().getId() == (item.getId()))
                    .toList();
            setDatesAndComments(item, bookingsForItem, allOwnerComments);
        }
        return returnedItems;
    }

    @Override
    public List<ItemDto> getSelection(String searchText) {
        if (searchText.isEmpty()) {
            return Collections.emptyList();
        }
        List<Item> items = itemRepository
                .findByNameIgnoreCaseContainingOrDescriptionIgnoreCaseContaining(searchText, searchText);

        return itemListMapper.toListDto(items.stream()
                .filter(Item::isAvailable)
                .toList()
        );
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CommentDto addComment(CommentCreationDto commentCreationDto) {
        List<Booking> bookings = bookingRepository.findAllByBookingAuthorIdAndBookingItemId(
                commentCreationDto.getAuthorId(), commentCreationDto.getItemId());

        Booking booking = bookings.stream().min(Comparator.comparing(Booking::getStart))
                .orElseThrow(() -> new BadRequestException("вещь или пользователь не найдена в БД"));
        if (booking.getStart().isAfter(LocalDateTime.now())) {
            log.warn("{} , current time:{}", booking.getStart(), LocalDateTime.now().plusSeconds(5));
            throw new BadRequestException("Автор не может оставлять комметнарии к вещи пока не забронирует её.");
        }
        Comment comment = itemMapper.toCommentModel(commentCreationDto);
        comment.setItem(bookings.getFirst().getBookingItem());
        comment.setAuthor(bookings.getFirst().getBookingAuthor());
        comment.setCreated(LocalDateTime.now());

        commentRepository.save(comment);
        return itemMapper.toCommentDto(comment);
    }
}
