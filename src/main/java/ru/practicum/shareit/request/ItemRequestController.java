package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
                                                         @RequestBody @Validated ItemRequestDto itemRequest) {
        log.info("{}[32m==> try to POST: addRequestItem, param: length description = {} <=={}[30m", (char) 27,
                itemRequest.getDescription(), (char) 27);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemRequestService.add(userId, itemRequest));
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getOwnRequests(@RequestHeader("X-Sharer-User-Id") String userId) {
        log.info("{}[32m ==> GET getOwnRequests, param: userId = {} <=={}[30m", (char) 27, userId, (char) 27);
        return ResponseEntity.ok().body(itemRequestService.getOwnRequests(userId));
    }

    @GetMapping(path = "/all")
    public ResponseEntity<List<ItemRequestDto>> getAllRequests(@RequestHeader("X-Sharer-User-Id") String userId) {
        log.info("{}[32m ==> GET getAllRequests, param: userId = {} <=={}[30m", (char) 27, userId, (char) 27);
        return ResponseEntity.ok().body(itemRequestService.getAllRequests(userId));
    }

    @GetMapping(path = "/requestId")
    public ResponseEntity<ItemRequestDto> getRequestItem(@RequestHeader("X-Sharer-User-Id") String userId,
                                                         @PathVariable long requestId) {
        log.info("{}[32m ==> GET getRequestItem, param: userId = {}, requestId = {} <=={}[30m",
                (char) 27, userId, requestId, (char) 27);
        return ResponseEntity.ok().body(itemRequestService.get(userId, requestId));
    }

}
