package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // поиск всех бронирований по автору бронирования
    List<Booking> findAllByBookingAuthorIdOrderByStartDesc(Long bookerId);

    // поиск всех бронирований по владельцу вещей
    List<Booking> findAllByBookingItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findAllByBookingItemOwnerIdOrderByStart(Long ownerId);

    List<Booking> findAllByBookingAuthorIdAndBookingItemId(Long ownerId, Long itemId);

    List<Booking> findAllByBookingItemId(Long itemId);


    // поиск текущих бронирований автора бронирования
    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.bookingItem AS i " +
            "JOIN b.bookingAuthor AS a " +
            "WHERE b.status LIKE 'APPROVED' " +
            "AND (b.start < :dateTime AND b.end > :dateTime) " +
            "AND a.id = :bookerId " +
            "ORDER BY b.start DESC ")
    List<Booking> findAllCurrentByBooker(Timestamp dateTime, Long bookerId);

    // поиск текущих бронирований владельца вещей
    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.bookingItem AS i " +
            "JOIN b.bookingAuthor AS a " +
            "WHERE b.status LIKE 'APPROVED' " +
            "AND (b.start < :dateTime AND b.end > :dateTime) " +
            "AND (i.owner.id = :ownerId) " +
            "ORDER BY b.start DESC ")
    List<Booking> findAllCurrentByOwner(Timestamp dateTime, Long ownerId);

    // поиск по статусу бронирования и автору бронирования (для WAITING и REJECTED)
    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.bookingItem AS i " +
            "JOIN b.bookingAuthor AS a " +
            "WHERE b.status LIKE :status " +
            "AND a.id = :bookerId " +
            "ORDER BY b.start DESC ")
    List<Booking> findAllByBookerWithStatus(String status, Long bookerId);

    // поиск по статусу бронирования и владельцу вещей (для WAITING и REJECTED)
    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.bookingItem AS i " +
            "JOIN b.bookingAuthor AS a " +
            "WHERE b.status LIKE :status " +
            "AND (i.owner.id = :ownerId) " +
            "ORDER BY b.start DESC ")
    List<Booking> findAllByOwnerWithStatus(String status, Long ownerId);

    // поиск будующих бронирований автора бронирования
    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.bookingItem AS i " +
            "JOIN b.bookingAuthor AS a " +
            "WHERE b.start > :start " +
            "AND a.id = :bookerId " +
            "ORDER BY b.start DESC ")
    List<Booking> findAllFutureByBooker(Timestamp start, Long bookerId);

    // поиск будующих бронирований владельца вещи
    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.bookingItem AS i " +
            "JOIN b.bookingAuthor AS a " +
            "WHERE b.start > :start " +
            "AND (i.owner.id = :ownerId) " +
            "ORDER BY b.start DESC ")
    List<Booking> findAllFutureByOwner(Timestamp start, Long ownerId);

    // поиск прошлых бронирований по автору бронирования
    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.bookingItem AS i " +
            "JOIN b.bookingAuthor AS a " +
            "WHERE b.end < :end " +
            "AND a.id = :bookerId " +
            "ORDER BY b.start DESC ")
    List<Booking> findAllPastByBooker(Timestamp end, Long bookerId);

    // поиск прошлых бронирований владельца вещей
    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.bookingItem AS i " +
            "JOIN b.bookingAuthor AS a " +
            "WHERE b.end < :end " +
            "AND (i.owner.id = :ownerId) " +
            "ORDER BY b.start DESC ")
    List<Booking> findAllPastByOwner(Timestamp end, Long ownerId);

}
