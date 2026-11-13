package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> bookingJson;
    @Autowired
    private JacksonTester<NewBookingDto> newBookingJson;

    @Test
    void bookingDtoShouldSerializeDatesAndNestedObjects() throws Exception {
        BookingDto dto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2026, 12, 1, 12, 0, 0))
                .end(LocalDateTime.of(2026, 12, 3, 12, 0, 0))
                .status(BookingStatus.APPROVED)
                .booker(UserDto.builder().id(2L).name("Маша").email("masha@mail.ru").build())
                .item(ItemDto.builder().id(3L).name("Дрель").description("Простая").available(true).build())
                .build();

        JsonContent<BookingDto> result = bookingJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-12-01T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-12-03T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Дрель");
    }

    @Test
    void newBookingDtoShouldParseDates() throws Exception {
        String json = "{\"itemId\":3,\"start\":\"2026-12-01T12:00:00\",\"end\":\"2026-12-03T12:00:00\"}";

        NewBookingDto dto = newBookingJson.parseObject(json);

        assertThat(dto.getItemId()).isEqualTo(3L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 12, 1, 12, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 12, 3, 12, 0, 0));
    }
}