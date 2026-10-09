package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerId(Long bookerId, Sort sort);

    List<Booking> findAllByBookerIdAndBookingStartTimeBeforeAndBookingFinishTimeAfter(
            Long bookerId, LocalDateTime now1, LocalDateTime now2, Sort sort);

    List<Booking> findAllByBookerIdAndBookingFinishTimeBefore(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findAllByBookerIdAndBookingStartTimeAfter(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findAllByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findAllByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findAllByItemOwnerIdAndBookingStartTimeBeforeAndBookingFinishTimeAfter(
            Long ownerId, LocalDateTime now1, LocalDateTime now2, Sort sort);

    List<Booking> findAllByItemOwnerIdAndBookingFinishTimeBefore(Long ownerId, LocalDateTime now, Sort sort);

    List<Booking> findAllByItemOwnerIdAndBookingStartTimeAfter(Long ownerId, LocalDateTime now, Sort sort);

    List<Booking> findAllByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    Optional<Booking> findFirstByItemIdAndStatusAndBookingStartTimeBeforeOrderByBookingStartTimeDesc(
            Long itemId, BookingStatus status, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndBookingStartTimeAfterOrderByBookingStartTimeAsc(
            Long itemId, BookingStatus status, LocalDateTime now);

    boolean existsByBookerIdAndItemIdAndStatusAndBookingFinishTimeBefore(
            Long bookerId, Long itemId, BookingStatus status, LocalDateTime now);
}
