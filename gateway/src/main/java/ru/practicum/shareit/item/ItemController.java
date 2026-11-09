package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;


@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final String OWNER_HEADER = "X-Sharer-User-Id";

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(OWNER_HEADER) Long ownerId,
                          @Valid @RequestBody ItemDto itemDto) {
        log.info("Получен запрос на создание вещи от пользователя id={}: {}", ownerId, itemDto);
        return itemClient.create(ownerId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(OWNER_HEADER) Long userId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody CommentDto commentDto) {
        log.info("Получен запрос на добавление отзыва к вещи id={} от пользователя id={}: {}", itemId, userId, commentDto);
        return itemClient.addComment(userId, itemId, commentDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(OWNER_HEADER) Long ownerId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("Получен запрос на обновление вещи id={} от пользователя id={}: {}", itemId, ownerId, itemDto);
        return itemClient.update(ownerId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findById(@RequestHeader(OWNER_HEADER) Long userId, @PathVariable Long itemId) {
        log.info("Получен запрос на получение вещи id={}", itemId);
        return itemClient.findById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> findAllByOwner(@RequestHeader(OWNER_HEADER) Long ownerId) {
        log.info("Получен запрос на получение списка вещей владельца id={}", ownerId);
        return itemClient.findAllByOwner(ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(OWNER_HEADER) Long userId, @RequestParam String text) {
        log.info("Получен запрос на поиск вещей по тексту: '{}'", text);
        return itemClient.search(userId, text);
    }
}
