package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;

    private UserDto owner;
    private UserDto booker;
    private UserDto stranger;
    private ItemDto item;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        owner = userService.create(UserDto.builder().name("Даша").email("dasha@mail.ru").build());
        booker = userService.create(UserDto.builder().name("Маша").email("masha@mail.ru").build());
        stranger = userService.create(UserDto.builder().name("Петя").email("petya@mail.ru").build());
        item = itemService.create(owner.getId(), ItemDto.builder()
                .name("Дрель").description("Простая").available(true).build());
        now = LocalDateTime.now();
    }

    private NewBookingDto bookingDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        NewBookingDto dto = new NewBookingDto();
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);
        return dto;
    }

    private BookingDto createFutureBooking() {
        return bookingService.create(booker.getId(),
                bookingDto(item.getId(), now.plusDays(1), now.plusDays(2)));
    }


    @Test
    void createShouldReturnWaitingBooking() {
        BookingDto booking = createFutureBooking();

        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(booking.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(booking.getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void createShouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class, () -> bookingService.create(999L,
                bookingDto(item.getId(), now.plusDays(1), now.plusDays(2))));
    }

    @Test
    void createShouldThrowWhenItemNotFound() {
        assertThrows(NotFoundException.class, () -> bookingService.create(booker.getId(),
                bookingDto(999L, now.plusDays(1), now.plusDays(2))));
    }

    @Test
    void createShouldThrowWhenItemUnavailable() {
        ItemDto unavailable = itemService.create(owner.getId(), ItemDto.builder()
                .name("Пила").description("Сломана").available(false).build());

        assertThrows(ValidationException.class, () -> bookingService.create(booker.getId(),
                bookingDto(unavailable.getId(), now.plusDays(1), now.plusDays(2))));
    }

    @Test
    void createShouldThrowWhenOwnerBooksOwnItem() {
        assertThrows(NotFoundException.class, () -> bookingService.create(owner.getId(),
                bookingDto(item.getId(), now.plusDays(1), now.plusDays(2))));
    }

    @Test
    void createShouldThrowWhenEndNotAfterStart() {
        assertThrows(ValidationException.class, () -> bookingService.create(booker.getId(),
                bookingDto(item.getId(), now.plusDays(2), now.plusDays(1))));
    }


    @Test
    void approveTrueShouldSetApproved() {
        BookingDto booking = createFutureBooking();

        BookingDto approved = bookingService.approve(owner.getId(), booking.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approveFalseShouldSetRejected() {
        BookingDto booking = createFutureBooking();

        BookingDto rejected = bookingService.approve(owner.getId(), booking.getId(), false);

        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approveShouldThrowWhenNotOwner() {
        BookingDto booking = createFutureBooking();

        assertThrows(ForbiddenException.class,
                () -> bookingService.approve(booker.getId(), booking.getId(), true));
    }

    @Test
    void approveShouldThrowWhenAlreadyProcessed() {
        BookingDto booking = createFutureBooking();
        bookingService.approve(owner.getId(), booking.getId(), true);

        assertThrows(ValidationException.class,
                () -> bookingService.approve(owner.getId(), booking.getId(), false));
    }

    @Test
    void approveShouldThrowWhenBookingNotFound() {
        assertThrows(NotFoundException.class,
                () -> bookingService.approve(owner.getId(), 999L, true));
    }


    @Test
    void findByIdShouldWorkForBookerAndOwner() {
        BookingDto booking = createFutureBooking();

        assertThat(bookingService.findById(booker.getId(), booking.getId()).getId())
                .isEqualTo(booking.getId());
        assertThat(bookingService.findById(owner.getId(), booking.getId()).getId())
                .isEqualTo(booking.getId());
    }

    @Test
    void findByIdShouldThrowForStranger() {
        BookingDto booking = createFutureBooking();

        assertThrows(NotFoundException.class,
                () -> bookingService.findById(stranger.getId(), booking.getId()));
    }

    private void createBookingsForAllStates() {
        BookingDto past = bookingService.create(booker.getId(),
                bookingDto(item.getId(), now.minusDays(3), now.minusDays(2)));
        bookingService.approve(owner.getId(), past.getId(), true);

        BookingDto current = bookingService.create(booker.getId(),
                bookingDto(item.getId(), now.minusHours(1), now.plusHours(1)));
        bookingService.approve(owner.getId(), current.getId(), true);

        bookingService.create(booker.getId(),
                bookingDto(item.getId(), now.plusDays(1), now.plusDays(2)));

        BookingDto rejected = bookingService.create(booker.getId(),
                bookingDto(item.getId(), now.plusDays(3), now.plusDays(4)));
        bookingService.approve(owner.getId(), rejected.getId(), false);
    }

    @Test
    void findAllByBookerShouldFilterByState() {
        createBookingsForAllStates();
        Long id = booker.getId();

        assertThat(bookingService.findAllByBooker(id, BookingState.ALL)).hasSize(4);
        assertThat(bookingService.findAllByBooker(id, BookingState.PAST)).hasSize(1);
        assertThat(bookingService.findAllByBooker(id, BookingState.CURRENT)).hasSize(1);
        assertThat(bookingService.findAllByBooker(id, BookingState.FUTURE)).hasSize(2);
        assertThat(bookingService.findAllByBooker(id, BookingState.WAITING)).hasSize(1);
        assertThat(bookingService.findAllByBooker(id, BookingState.REJECTED)).hasSize(1);
    }

    @Test
    void findAllByOwnerShouldFilterByState() {
        createBookingsForAllStates();
        Long id = owner.getId();

        assertThat(bookingService.findAllByOwner(id, BookingState.ALL)).hasSize(4);
        assertThat(bookingService.findAllByOwner(id, BookingState.PAST)).hasSize(1);
        assertThat(bookingService.findAllByOwner(id, BookingState.CURRENT)).hasSize(1);
        assertThat(bookingService.findAllByOwner(id, BookingState.FUTURE)).hasSize(2);
        assertThat(bookingService.findAllByOwner(id, BookingState.WAITING)).hasSize(1);
        assertThat(bookingService.findAllByOwner(id, BookingState.REJECTED)).hasSize(1);
    }

    @Test
    void listsShouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> bookingService.findAllByBooker(999L, BookingState.ALL));
        assertThrows(NotFoundException.class,
                () -> bookingService.findAllByOwner(999L, BookingState.ALL));
    }
}