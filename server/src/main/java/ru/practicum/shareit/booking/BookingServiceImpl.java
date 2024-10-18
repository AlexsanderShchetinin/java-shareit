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

import java.time.LocalDateTime;
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
        if (Objects.equals(returnedItem.getOwner().getId(), bookerId)) {
            throw new BadRequestException("Владелец не может бронировать собственные вещи");
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
            throw new InterruptionRuleException("Изменять статус бронирования может только владелец вещи");
        }
        if (bookingStatusDto.getApprove()) {
            returnedBooking.setStatus(BookingStatus.APPROVED);
        } else {
            returnedBooking.setStatus(BookingStatus.REJECTED);
        }
        bookingRepository.save(returnedBooking);
        return bookingMapper.toDto(returnedBooking);
    }

    // получение бронирования по id
    // Получить информацию о бронировании могут только владелец вещи или автор бронирования
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

    // Получение списка всех бронирований текущего пользователя с ранжированием по статусам
    @Override
    public List<BookingDto> getBookingsByBooker(String bookerStr, String state) {
        Long bookerId = Long.parseLong(bookerStr);
        userRepository.findById(bookerId)
                .orElseThrow(() -> new MyNotFoundException("Пользователя с id=" + bookerId + " не существует."));
        return switch (convertToState(state)) {
            case CURRENT -> bookingRepository.findAllCurrentByBooker(LocalDateTime.now(), bookerId).stream()
                    .map(bookingMapper::toDto)
                    .toList();
            case WAITING -> bookingRepository.findAllByBookerWithStatus(BookingStatus.WAITING, bookerId)
                    .stream()
                    .map(bookingMapper::toDto)
                    .toList();
            case PAST -> bookingRepository.findAllPastByBooker(LocalDateTime.now(), bookerId).stream()
                    .map(bookingMapper::toDto)
                    .toList();
            case FUTURE -> bookingRepository.findAllFutureByBooker(LocalDateTime.now(), bookerId).stream()
                    .map(bookingMapper::toDto)
                    .toList();
            case REJECTED -> bookingRepository
                    .findAllByBookerWithStatus(BookingStatus.REJECTED, bookerId).stream()
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
                case CURRENT -> bookingRepository.findAllCurrentByOwner(LocalDateTime.now(), ownerId).stream()
                        .map(bookingMapper::toDto)
                        .toList();
                case WAITING -> bookingRepository.findAllByOwnerWithStatus(BookingStatus.WAITING, ownerId)
                        .stream()
                        .map(bookingMapper::toDto)
                        .toList();
                case PAST -> bookingRepository.findAllPastByOwner(LocalDateTime.now(), ownerId).stream()
                        .map(bookingMapper::toDto)
                        .toList();
                case FUTURE -> bookingRepository.findAllFutureByOwner(LocalDateTime.now(), ownerId).stream()
                        .map(bookingMapper::toDto)
                        .toList();
                case REJECTED -> bookingRepository
                        .findAllByOwnerWithStatus(BookingStatus.REJECTED, ownerId).stream()
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
