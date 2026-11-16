package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void shouldSerializeCreatedAndItems() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна палатка")
                .created(LocalDateTime.of(2026, 11, 9, 15, 22, 6))
                .items(List.of(ItemShortDto.builder().id(2L).name("Палатка").ownerId(5L).build()))
                .build();

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Нужна палатка");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-11-09T15:22:06");
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Палатка");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(5);
    }

    @Test
    void shouldParseDescription() throws Exception {
        ItemRequestDto dto = json.parseObject("{\"description\":\"Нужна палатка\"}");

        assertThat(dto.getDescription()).isEqualTo("Нужна палатка");
    }
}