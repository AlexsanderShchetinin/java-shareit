package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findAllByRequestOwnerId(long userId);
    // ItemRequest findById(long userId);

    List<ItemRequest> findAllByRequestOwnerIdNot(long userId);

}
