package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.MyNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestListMapperImpl;
import ru.practicum.shareit.request.mapper.ItemRequestMapperImpl;
import ru.practicum.shareit.request.mapper.ItemResponseListMapperImpl;
import ru.practicum.shareit.request.resp.ItemResponse;
import ru.practicum.shareit.request.resp.ItemResponseRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository repository;
    private final UserRepository userRepository;
    private final ItemResponseRepository responseRepository;

    private final ItemRequestMapperImpl requestMapper;
    private final ItemRequestListMapperImpl requestListMapper;
    private final ItemResponseListMapperImpl responseListMapper;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public ItemRequestDto add(String ownerStr, ItemRequestDto itemRequestDto) {
        long userId = Long.parseLong(ownerStr);

        User requestOwner = userRepository.findById(userId)
                .orElseThrow(
                        () -> new MyNotFoundException("Пользователь с id=" + userId + " не зарегистрирован"));

        ItemRequest itemRequest = requestMapper.toModel(itemRequestDto);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequestOwner(requestOwner);

        return requestMapper.toDto(repository.save(itemRequest));
    }

    @Override
    public ItemRequestDto get(String userStr, long requestId) {
        long userId = Long.parseLong(userStr);
        userRepository.findById(userId)
                .orElseThrow(
                        () -> new MyNotFoundException("Пользователь с id=" + userId + " не зарегистрирован"));

        // получаем запрос по его id и преобразуем в dto
        ItemRequest request = repository.findById(requestId).orElseThrow(
                () -> new MyNotFoundException("Запроса с id=" + requestId + " не существует!"));
        ItemRequestDto requestDto = requestMapper.toDto(request);

        // находим все ответы на запрос и прикрепляем к dto
        List<ItemResponse> responses = responseRepository.findAllByRequestId(request.getId());
        List<ItemResponseDto> itemResponseDto = responseListMapper.toListDto(responses);
        requestDto.setItems(itemResponseDto);

        return requestDto;
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(String userStr) {
        long userId = Long.parseLong(userStr);
        // Валидация пользователя
        userRepository.findById(userId)
                .orElseThrow(
                        () -> new MyNotFoundException("Пользователь с id=" + userId + " не зарегистрирован"));

        // получаем все запросы по пользователю
        List<ItemRequestDto> requests = requestListMapper.toListDto(
                repository.findAllByRequestOwnerId(userId));

        // по всем id запросов ищем список с ответами
        for (ItemRequestDto request : requests) {
            List<ItemResponse> responses = responseRepository.findAllByRequestId(request.getId());
            List<ItemResponseDto> itemResponseDto = responseListMapper.toListDto(responses);
            request.setItems(itemResponseDto);
        }

        // сортировка запросов по времени в порядке убывания
        return requests.stream()
                .sorted(Comparator.comparing(ItemRequestDto::getCreated).reversed())
                .toList();
    }

    @Override
    public List<ItemRequestDto> getAllRequests(String userStr) {
        long userId = Long.parseLong(userStr);
        // Валидация пользователя
        userRepository.findById(userId)
                .orElseThrow(
                        () -> new MyNotFoundException("Пользователь с id=" + userId + " не зарегистрирован"));

        // получаем все запросы исключая пользователя
        List<ItemRequestDto> requests = requestListMapper.toListDto(
                repository.findAllByRequestOwnerIdNot(userId));

        // по всем id запросов ищем список с ответами
        for (ItemRequestDto request : requests) {
            List<ItemResponse> responses = responseRepository.findAllByRequestId(request.getId());
            List<ItemResponseDto> itemResponseDto = responseListMapper.toListDto(responses);
            request.setItems(itemResponseDto);
        }

        // сортировка запросов по времени в порядке убывания
        return requests.stream()
                .sorted(Comparator.comparing(ItemRequestDto::getCreated).reversed())
                .toList();

    }
}
