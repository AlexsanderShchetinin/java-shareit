package ru.practicum.shareit.request.dto;

public record ItemResponse(long itemId, String itemName, long ownerId, String textResponse) {
}
