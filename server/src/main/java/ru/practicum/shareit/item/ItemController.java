package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final String OWNER_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader(OWNER_HEADER) Long ownerId,
                          @Valid @RequestBody ItemDto itemDto) {
        log.info("Получен запрос на создание вещи от пользователя id={}: {}", ownerId, itemDto);
        return itemService.create(ownerId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(OWNER_HEADER) Long userId,
                              @PathVariable Long itemId,
                              @Valid @RequestBody CommentDto commentDto) {
        log.info("Получен запрос на добавление отзыва к вещи id={} от пользователя id={}: {}", itemId, userId, commentDto);
        return itemService.addComment(userId, itemId, commentDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(OWNER_HEADER) Long ownerId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("Получен запрос на обновление вещи id={} от пользователя id={}: {}", itemId, ownerId, itemDto);
        return itemService.update(ownerId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@PathVariable Long itemId) {
        log.info("Получен запрос на получение вещи id={}", itemId);
        return itemService.findById(itemId);
    }

    @GetMapping
    public List<ItemDto> findAllByOwner(@RequestHeader(OWNER_HEADER) Long ownerId) {
        log.info("Получен запрос на получение списка вещей владельца id={}", ownerId);
        return itemService.findAllByOwner(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.info("Получен запрос на поиск вещей по тексту: '{}'", text);
        return itemService.search(text);
    }
}