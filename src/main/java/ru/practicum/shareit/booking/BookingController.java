package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                             @Valid @RequestBody NewBookingDto dto) {
        log.info("Получен запрос на создание бронирования от пользователя id={}: {}", userId, dto);
        return bookingService.create(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(USER_ID_HEADER) Long ownerId,
                              @PathVariable Long bookingId,
                              @RequestParam boolean approved) {
        log.info("Получен запрос на изменение бронирования от пользователя id={}: {}", ownerId, bookingId);
        return bookingService.approve(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findById(@RequestHeader(USER_ID_HEADER) Long userId,
                               @PathVariable Long bookingId) {
        log.info("Получен запрос на получение бронирования id={}", bookingId);
        return bookingService.findById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> findAllByBooker(@RequestHeader(USER_ID_HEADER) Long userId,
                                            @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("Получен запрос на получение всех бронирований пользователя id={}: {}", userId, state);
        return bookingService.findAllByBooker(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> findAllByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                           @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("Получен запрос на получение всех бронирований владельца id={}: {}", ownerId, state);
        return bookingService.findAllByOwner(ownerId, state);
    }
}





