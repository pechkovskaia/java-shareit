package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "bookingStartTime");

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto create(Long userId, NewBookingDto dto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id " + dto.getItemId() + " не найдена"));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        if (item.getOwnerId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }
        if (!dto.getEnd().isAfter(dto.getStart())) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }

        Booking booking = BookingMapper.toBooking(dto, item, booker);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("Подтвердить бронирование может только владелец вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto findById(Long userId, Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwnerId().equals(userId);
        if (!isBooker && !isOwner) {
            throw new NotFoundException("Бронирование с id " + bookingId + " не найдено");
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> findAllByBooker(Long userId, BookingState state) {
        checkUserExists(userId);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByBookerId(userId, NEWEST_FIRST);
            case CURRENT -> bookingRepository
                    .findAllByBookerIdAndBookingStartTimeBeforeAndBookingFinishTimeAfter(userId, now, now, NEWEST_FIRST);
            case PAST -> bookingRepository.findAllByBookerIdAndBookingFinishTimeBefore(userId, now, NEWEST_FIRST);
            case FUTURE -> bookingRepository.findAllByBookerIdAndBookingStartTimeAfter(userId, now, NEWEST_FIRST);
            case WAITING -> bookingRepository.findAllByBookerIdAndStatus(userId, BookingStatus.WAITING, NEWEST_FIRST);
            case REJECTED -> bookingRepository.findAllByBookerIdAndStatus(userId, BookingStatus.REJECTED, NEWEST_FIRST);
        };
        return bookings.stream().map(BookingMapper::toBookingDto).toList();
    }

    @Override
    public List<BookingDto> findAllByOwner(Long ownerId, BookingState state) {
        checkUserExists(ownerId);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByItemOwnerId(ownerId, NEWEST_FIRST);
            case CURRENT -> bookingRepository
                    .findAllByItemOwnerIdAndBookingStartTimeBeforeAndBookingFinishTimeAfter(ownerId, now, now, NEWEST_FIRST);
            case PAST -> bookingRepository.findAllByItemOwnerIdAndBookingFinishTimeBefore(ownerId, now, NEWEST_FIRST);
            case FUTURE -> bookingRepository.findAllByItemOwnerIdAndBookingStartTimeAfter(ownerId, now, NEWEST_FIRST);
            case WAITING -> bookingRepository.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, NEWEST_FIRST);
            case REJECTED -> bookingRepository.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, NEWEST_FIRST);
        };
        return bookings.stream().map(BookingMapper::toBookingDto).toList();
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }
}