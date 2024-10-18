package ru.practicum.shareit.request.resp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemResponseRepository extends JpaRepository<ItemResponse, Long> {

    List<ItemResponse> findAllByRequestId(long requestId);

}
