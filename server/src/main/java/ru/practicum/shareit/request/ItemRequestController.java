package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader(USER_ID_HEADER) Long userId, @Valid @RequestBody ItemRequestDto dto) {
        log.info("Получен запрос на создание запроса на вещь от пользователя id={}: {}", userId, dto);
        return itemRequestService.create(userId, dto);
    }


    @GetMapping
    public List<ItemRequestDto> findOwn(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получен запрос на получение своих запросов на вещи пользователя id={}", userId);
        return itemRequestService.findOwn(userId);

    }


    @GetMapping("/all")
    public List<ItemRequestDto> findAllOthers(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получен запрос на получение запросов других пользователей для пользователя id={}", userId);
        return itemRequestService.findAllOthers(userId);
    }


    @GetMapping("/{requestId}")
    public ItemRequestDto findById(@RequestHeader(USER_ID_HEADER) Long userId, @PathVariable Long requestId) {

        log.info("Получен запрос на получение запроса на вещь id={}", requestId);
        return itemRequestService.findById(userId, requestId);

    }

}


