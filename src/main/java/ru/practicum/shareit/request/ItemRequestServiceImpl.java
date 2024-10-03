package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public class ItemRequestServiceImpl implements ItemRequestService {
    @Override
    public ItemRequestDto add(String ownerStr, ItemRequestDto itemRequestDto) {
        long userId = Long.parseLong(ownerStr);
        // TODO
        return null;
    }

    @Override
    public ItemRequestDto get(String userStr, long requestId) {
        long userId = Long.parseLong(userStr);
        // TODO
        return null;
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(String userStr) {
        long userId = Long.parseLong(userStr);
        // TODO
        return List.of();
    }

    @Override
    public List<ItemRequestDto> getAllRequests(String userStr) {
        long userId = Long.parseLong(userStr);
        // TODO
        return List.of();
    }
}
