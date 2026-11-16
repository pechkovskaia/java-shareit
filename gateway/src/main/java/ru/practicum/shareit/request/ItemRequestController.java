package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_ID_HEADER) Long userId, @Valid @RequestBody ItemRequestDto dto) {
        log.info("Получен запрос на создание запроса на вещь от пользователя id={}: {}", userId, dto);
        return itemRequestClient.create(userId, dto);
    }


    @GetMapping
    public ResponseEntity<Object> findOwn(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получен запрос на получение своих запросов на вещи пользователя id={}", userId);
        return itemRequestClient.findOwn(userId);

    }


    @GetMapping("/all")
    public ResponseEntity<Object> findAllOthers(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получен запрос на получение запросов других пользователей для пользователя id={}", userId);
        return itemRequestClient.findAllOthers(userId);
    }


    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findById(@RequestHeader(USER_ID_HEADER) Long userId, @PathVariable Long requestId) {

        log.info("Получен запрос на получение запроса на вещь id={}", requestId);
        return itemRequestClient.findById(userId, requestId);

    }

}
