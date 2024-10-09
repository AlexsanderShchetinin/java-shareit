package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;


public interface ItemRequestService {

    ItemRequestDto add(String userId, ItemRequestDto itemRequestDto);

    ItemRequestDto get(String userId, long requestId);

    List<ItemRequestDto> getOwnRequests(String userId);

    List<ItemRequestDto> getAllRequests(String userId);  // except own requests
}
