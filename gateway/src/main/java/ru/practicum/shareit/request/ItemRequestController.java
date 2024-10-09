package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.validator.Marker;

/**
 * Created by Shchetinin Alexander 03.10.2024
 */

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final RequestClient requestClient;


    @PostMapping
    public ResponseEntity<Object> addRequestItem(@RequestHeader("X-Sharer-User-Id") String userStr,
                                                 @RequestBody
                                                 @Validated(Marker.Create.class) ItemRequestDto itemRequest) {
        long userId;
        try {
            userId = Long.parseLong(userStr);
        } catch (NumberFormatException e) {
            log.warn("{}[31m ==> POST/requests (METHOD addRequestItem) <== catch except: {} {}[37m",
                    (char) 27, e.getMessage() + e.getCause(), (char) 27);
            throw new BadRequestException("ошибка преобразования Id владельца:" + e.getMessage());
        }

        log.info("{}[32m==> try to POST: addRequestItem, param: length description = {} <=={}[37m", (char) 27,
                itemRequest.getDescription(), (char) 27);
        return requestClient.add(userId, itemRequest);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnRequests(@RequestHeader("X-Sharer-User-Id") String userStr) {
        long userId;
        try {
            userId = Long.parseLong(userStr);
        } catch (NumberFormatException e) {
            log.warn("{}[31m ==> GET/requests (METHOD getOwnRequests) <== catch except: {} {}[37m",
                    (char) 27, e.getMessage() + e.getCause(), (char) 27);
            throw new BadRequestException("ошибка преобразования Id владельца:" + e.getMessage());
        }
        log.info("{}[32m ==> GET getOwnRequests, param: userId = {} <=={}[37m", (char) 27, userId, (char) 27);
        return requestClient.getOwnRequests(userId);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") String userStr) {
        long userId;
        try {
            userId = Long.parseLong(userStr);
        } catch (NumberFormatException e) {
            log.warn("{}[31m ==> GET/requests/all (METHOD getAllRequests) <== catch except: {} {}[37m",
                    (char) 27, e.getMessage() + e.getCause(), (char) 27);
            throw new BadRequestException("ошибка преобразования Id владельца:" + e.getMessage());
        }
        log.info("{}[32m ==> GET getAllRequests, param: userId = {} <=={}[37m", (char) 27, userId, (char) 27);
        return requestClient.getAllRequests(userId);
    }

    @GetMapping(path = "/{requestId}")
    public ResponseEntity<Object> getRequestItem(@RequestHeader("X-Sharer-User-Id") String userStr,
                                                 @PathVariable long requestId) {
        long userId;
        try {
            userId = Long.parseLong(userStr);
        } catch (NumberFormatException e) {
            log.warn("{}[31m ==> GET/requests/{requestId} (METHOD getRequestItem) <== catch except: {} {}[37m",
                    (char) 27, e.getMessage() + e.getCause(), (char) 27);
            throw new BadRequestException("ошибка преобразования Id владельца:" + e.getMessage());
        }
        log.info("{}[32m ==> GET getRequestItem, param: userId = {}, requestId = {} <=={}[37m",
                (char) 27, userId, requestId, (char) 27);
        return requestClient.getRequest(userId, requestId);
    }

}
