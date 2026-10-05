package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;

import java.util.List;

public interface BookingService {

    BookingDto create(Long userId, NewBookingDto dto);

    BookingDto approve(Long ownerId, Long bookingId, boolean approved);

    BookingDto findById(Long userId, Long bookingId);

    List<BookingDto> findAllByBooker(Long userId, BookingState state);

    List<BookingDto> findAllByOwner(Long ownerId, BookingState state);

}
