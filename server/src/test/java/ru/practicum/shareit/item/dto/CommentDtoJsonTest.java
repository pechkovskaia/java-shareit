package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void shouldSerializeCreated() throws Exception {
        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("Отличная дрель")
                .authorName("Маша")
                .created(LocalDateTime.of(2026, 11, 10, 18, 30, 0))
                .build();

        JsonContent<CommentDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Отличная дрель");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Маша");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-11-10T18:30:00");
    }
}