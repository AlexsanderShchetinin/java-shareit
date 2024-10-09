package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

/**
 * Created by Shchetinin Alexander 03.10.2024
 */

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;


    @PostMapping
    public ResponseEntity<ItemRequestDto> addRequestItem(@RequestHeader("X-Sharer-User-Id") String userId,
                                                         @RequestBody ItemRequestDto itemRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemRequestService.add(userId, itemRequest));
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getOwnRequests(@RequestHeader("X-Sharer-User-Id") String userId) {
        return ResponseEntity.ok().body(itemRequestService.getOwnRequests(userId));
    }

    @GetMapping(path = "/all")
    public ResponseEntity<List<ItemRequestDto>> getAllRequests(@RequestHeader("X-Sharer-User-Id") String userId) {
        return ResponseEntity.ok().body(itemRequestService.getAllRequests(userId));
    }

    @GetMapping(path = "/{requestId}")
    public ResponseEntity<ItemRequestDto> getRequestItem(@RequestHeader("X-Sharer-User-Id") String userId,
                                                         @PathVariable long requestId) {
        return ResponseEntity.ok().body(itemRequestService.get(userId, requestId));
    }

}
