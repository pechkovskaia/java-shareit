package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;

    private UserDto owner;
    private UserDto booker;

    @BeforeEach
    void setUp() {
        owner = userService.create(UserDto.builder().name("Даша").email("dasha@mail.ru").build());
        booker = userService.create(UserDto.builder().name("Маша").email("masha@mail.ru").build());
    }

    private ItemDto createItem(String name, boolean available) {
        return itemService.create(owner.getId(), ItemDto.builder()
                .name(name).description("Описание: " + name).available(available).build());
    }

    private BookingDto createApprovedBooking(Long itemId, LocalDateTime start, LocalDateTime end) {
        NewBookingDto dto = new NewBookingDto();
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);
        BookingDto booking = bookingService.create(booker.getId(), dto);
        return bookingService.approve(owner.getId(), booking.getId(), true);
    }


    @Test
    void createShouldReturnItem() {
        ItemDto item = createItem("Дрель", true);

        assertThat(item.getId()).isNotNull();
        assertThat(item.getName()).isEqualTo("Дрель");
        assertThat(item.getAvailable()).isTrue();
    }

    @Test
    void createShouldThrowWhenUserNotFound() {
        ItemDto dto = ItemDto.builder().name("Дрель").description("Простая").available(true).build();

        assertThrows(NotFoundException.class, () -> itemService.create(999L, dto));
    }

    @Test
    void createShouldThrowWhenRequestNotFound() {
        ItemDto dto = ItemDto.builder().name("Дрель").description("Простая")
                .available(true).requestId(999L).build();

        assertThrows(NotFoundException.class, () -> itemService.create(owner.getId(), dto));
    }


    @Test
    void updateShouldChangeOnlySentFields() {
        ItemDto item = createItem("Дрель", true);

        ItemDto updated = itemService.update(owner.getId(), item.getId(),
                ItemDto.builder().name("Новая дрель").build());

        assertThat(updated.getName()).isEqualTo("Новая дрель");
        assertThat(updated.getDescription()).isEqualTo("Описание: Дрель"); // не изменилось
        assertThat(updated.getAvailable()).isTrue();                        // не изменилось
    }

    @Test
    void updateShouldThrowWhenNotOwner() {
        ItemDto item = createItem("Дрель", true);

        assertThrows(ForbiddenException.class, () -> itemService.update(booker.getId(), item.getId(),
                ItemDto.builder().name("Моя дрель").build()));
    }

    @Test
    void updateShouldThrowWhenItemNotFound() {
        assertThrows(NotFoundException.class, () -> itemService.update(owner.getId(), 999L,
                ItemDto.builder().name("Дрель").build()));
    }


    @Test
    void findByIdShouldThrowWhenNotFound() {
        assertThrows(NotFoundException.class, () -> itemService.findById(999L));
    }


    @Test
    void findAllByOwnerShouldReturnLastAndNextBookings() {
        ItemDto item = createItem("Дрель", true);
        LocalDateTime now = LocalDateTime.now();
        BookingDto past = createApprovedBooking(item.getId(), now.minusDays(3), now.minusDays(2));
        BookingDto future = createApprovedBooking(item.getId(), now.plusDays(2), now.plusDays(3));

        List<ItemDto> items = itemService.findAllByOwner(owner.getId());

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getLastBooking().getId()).isEqualTo(past.getId());
        assertThat(items.get(0).getNextBooking().getId()).isEqualTo(future.getId());
    }

    @Test
    void findAllByOwnerShouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class, () -> itemService.findAllByOwner(999L));
    }


    @Test
    void searchShouldFindOnlyAvailableIgnoringCase() {
        createItem("Дрель", true);
        createItem("Дрель сломанная", false); // недоступная — не должна найтись

        List<ItemDto> found = itemService.search("дРеЛь");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Дрель");
    }

    @Test
    void searchShouldReturnEmptyForBlankText() {
        createItem("Дрель", true);

        assertThat(itemService.search("")).isEmpty();
    }


    @Test
    void addCommentShouldSaveWhenUserRentedItem() {
        ItemDto item = createItem("Дрель", true);
        LocalDateTime now = LocalDateTime.now();
        createApprovedBooking(item.getId(), now.minusDays(3), now.minusDays(2)); // аренда закончилась

        CommentDto comment = itemService.addComment(booker.getId(), item.getId(),
                CommentDto.builder().text("Отличная дрель").build());

        assertThat(comment.getText()).isEqualTo("Отличная дрель");
        assertThat(comment.getAuthorName()).isEqualTo("Маша");

        ItemDto found = itemService.findById(item.getId());
        assertThat(found.getComments()).hasSize(1);
    }

    @Test
    void addCommentShouldThrowWhenUserDidNotRent() {
        ItemDto item = createItem("Дрель", true);

        assertThrows(ValidationException.class, () -> itemService.addComment(booker.getId(), item.getId(),
                CommentDto.builder().text("Отличная дрель").build()));
    }
}