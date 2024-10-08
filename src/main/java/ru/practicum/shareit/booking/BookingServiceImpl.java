package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreatingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStatusDto;
import ru.practicum.shareit.booking.mapper.BookingMapperImpl;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.InterruptionRuleException;
import ru.practicum.shareit.exception.MyNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapperImpl bookingMapper;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingDto create(String bookerStr, BookingCreatingDto bookingCreatingDto) {
        Long bookerId = Long.parseLong(bookerStr);
        if (bookingCreatingDto.getStart().isAfter(bookingCreatingDto.getEnd()) ||
                bookingCreatingDto.getStart().equals(bookingCreatingDto.getEnd())) {
            throw new BadRequestException("Дата окончания бронирования должна быть позже даты начала");
        }
        User returnedUser = userRepository.findById(bookerId)
                .orElseThrow(() -> new MyNotFoundException("Пользователя с id=" + bookerId + " не существует."));
        Item returnedItem = itemRepository.findById(bookingCreatingDto.getItemId())
                .orElseThrow(() -> new MyNotFoundException("Вещи с id=" + bookingCreatingDto.getItemId() + " не существует."));

        if (!returnedItem.isAvailable()) {
            throw new BadRequestException("Вещь на данный момент недоступна для бронирования");
        }
        Booking booking = bookingMapper.toModel(bookingCreatingDto);

        booking.setBookingAuthor(returnedUser);
        booking.setBookingItem(returnedItem);
        booking.setStatus(BookingStatus.WAITING);

        bookingRepository.save(booking);
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingDto changeStatus(String ownerStr, BookingStatusDto bookingStatusDto) {
        Long ownerId = Long.parseLong(ownerStr);
        userRepository.findById(ownerId)
                .orElseThrow(() -> new BadRequestException("Владельца с id=" + ownerId + " не существует."));
        Booking returnedBooking = bookingRepository.findById(bookingStatusDto.getId())
                .orElseThrow(() ->
                        new BadRequestException("Бронирования с id=" + bookingStatusDto.getId() + " не существует."));

        if (!returnedBooking.getBookingItem().getOwner().getId().equals(ownerId)) {
            throw new InterruptionRuleException("Изменять статус может только владелец вещи");
        }
        if (bookingStatusDto.approve) {
            returnedBooking.setStatus(BookingStatus.APPROVED);
        } else {
            returnedBooking.setStatus(BookingStatus.REJECTED);
        }
        bookingRepository.save(returnedBooking);
        Item item = returnedBooking.getBookingItem();
        System.out.println(item);
        return bookingMapper.toDto(returnedBooking);
    }

    @Override
    public BookingDto getById(String userStr, long bookingId) {
        Long userId = Long.parseLong(userStr);
        userRepository.findById(userId)
                .orElseThrow(() -> new MyNotFoundException("Владельца с id=" + userId + " не существует."));
        Booking returnedBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new MyNotFoundException("Бронирования с id=" + bookingId + " не существует."));

        if (Objects.equals(returnedBooking.getBookingItem().getOwner().getId(), userId) ||
                Objects.equals(returnedBooking.getBookingAuthor().getId(), userId)) {
            return bookingMapper.toDto(returnedBooking);
        }
        throw new InterruptionRuleException("Получить информацию о бронировании могут только владелец вещи " +
                "или автор бронирования.");
    }

    @Override
    public List<BookingDto> getBookingsByBooker(String bookerStr, String state) {
        Long bookerId = Long.parseLong(bookerStr);
        userRepository.findById(bookerId)
                .orElseThrow(() -> new MyNotFoundException("Пользователя с id=" + bookerId + " не существует."));
        return switch (convertToState(state)) {
            case CURRENT -> bookingRepository.findAllCurrentByBooker(Timestamp.from(Instant.now()),
                            bookerId).stream()
                    .map(bookingMapper::toDto)
                    .toList();
            case WAITING -> bookingRepository.findAllByBookerWithStatus(StateStatus.WAITING.toString(), bookerId)
                    .stream()
                    .map(bookingMapper::toDto)
                    .toList();
            case PAST -> bookingRepository.findAllPastByBooker(Timestamp.from(Instant.now()), bookerId).stream()
                    .map(bookingMapper::toDto)
                    .toList();
            case FUTURE -> bookingRepository.findAllFutureByBooker(Timestamp.from(Instant.now()), bookerId).stream()
                    .map(bookingMapper::toDto)
                    .toList();
            case REJECTED -> bookingRepository
                    .findAllByBookerWithStatus(StateStatus.REJECTED.toString(), bookerId).stream()
                    .map(bookingMapper::toDto)
                    .toList();
            // default = ALL
            default -> bookingRepository.findAllByBookingAuthorIdOrderByStartDesc(bookerId).stream()
                    .map(bookingMapper::toDto)
                    .toList();
        };
    }

    @Override
    public List<BookingDto> getBookingsByOwner(String ownerStr, String state) {
        Long ownerId = Long.parseLong(ownerStr);
        userRepository.findById(ownerId)
                .orElseThrow(() -> new MyNotFoundException("Владельца с id=" + ownerId + " не существует."));
        if (!itemRepository.findAllByOwnerId(ownerId).isEmpty()) {
            return switch (convertToState(state)) {
                case CURRENT -> bookingRepository.findAllCurrentByOwner(Timestamp.from(Instant.now()), ownerId).stream()
                        .map(bookingMapper::toDto)
                        .toList();
                case WAITING -> bookingRepository.findAllByOwnerWithStatus(StateStatus.WAITING.toString(), ownerId)
                        .stream()
                        .map(bookingMapper::toDto)
                        .toList();
                case PAST -> bookingRepository.findAllPastByOwner(Timestamp.from(Instant.now()), ownerId).stream()
                        .map(bookingMapper::toDto)
                        .toList();
                case FUTURE -> bookingRepository.findAllFutureByOwner(Timestamp.from(Instant.now()), ownerId).stream()
                        .map(bookingMapper::toDto)
                        .toList();
                case REJECTED -> bookingRepository
                        .findAllByOwnerWithStatus(StateStatus.REJECTED.toString(), ownerId).stream()
                        .map(bookingMapper::toDto)
                        .toList();
                // default = ALL
                default -> bookingRepository.findAllByBookingItemOwnerIdOrderByStartDesc(ownerId).stream()
                        .map(bookingMapper::toDto)
                        .toList();
            };
        }
        return List.of();
    }


    private StateStatus convertToState(String state) {
        if (state.equalsIgnoreCase("CURRENT")) {
            return StateStatus.CURRENT;
        }
        if (state.equalsIgnoreCase("PAST")) {
            return StateStatus.PAST;
        }
        if (state.equalsIgnoreCase("FUTURE")) {
            return StateStatus.FUTURE;
        }
        if (state.equalsIgnoreCase("WAITING")) {
            return StateStatus.WAITING;
        }
        if (state.equalsIgnoreCase("REJECTED")) {
            return StateStatus.REJECTED;
        }
        if (state.equalsIgnoreCase("ALL")) {
            return StateStatus.ALL;
        } else {
            throw new InterruptionRuleException("Параметр state в URL запросе некоректный");
        }
    }
}
