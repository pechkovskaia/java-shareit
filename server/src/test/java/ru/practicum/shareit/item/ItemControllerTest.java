package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemService itemService;

    private final ItemDto drill = ItemDto.builder()
            .id(1L).name("Дрель").description("Простая").available(true).build();

    @Test
    void createShouldReturnItem() throws Exception {
        when(itemService.create(eq(1L), any(ItemDto.class))).thenReturn(drill);

        mvc.perform(post("/items")
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(drill)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void updateShouldReturnItem() throws Exception {
        when(itemService.update(eq(1L), eq(1L), any(ItemDto.class))).thenReturn(drill);

        mvc.perform(patch("/items/1")
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(ItemDto.builder().name("Дрель").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void updateShouldReturn403WhenNotOwner() throws Exception {
        when(itemService.update(anyLong(), anyLong(), any(ItemDto.class)))
                .thenThrow(new ForbiddenException("Редактировать вещь может только её владелец"));

        mvc.perform(patch("/items/1")
                        .header(HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(ItemDto.builder().name("Моя").build())))
                .andExpect(status().isForbidden());
    }

    @Test
    void findByIdShouldReturnItem() throws Exception {
        when(itemService.findById(1L)).thenReturn(drill);

        mvc.perform(get("/items/1").header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void findAllByOwnerShouldReturnList() throws Exception {
        when(itemService.findAllByOwner(1L)).thenReturn(List.of(drill));

        mvc.perform(get("/items").header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void searchShouldReturnList() throws Exception {
        when(itemService.search("дрель")).thenReturn(List.of(drill));

        mvc.perform(get("/items/search").param("text", "дрель").header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void addCommentShouldReturnComment() throws Exception {
        CommentDto comment = CommentDto.builder().id(1L).text("Отличная").authorName("Маша").build();
        when(itemService.addComment(eq(2L), eq(1L), any(CommentDto.class))).thenReturn(comment);

        mvc.perform(post("/items/1/comment")
                        .header(HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CommentDto.builder().text("Отличная").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Отличная"))
                .andExpect(jsonPath("$.authorName").value("Маша"));
    }

    @Test
    void addCommentShouldReturn400WhenNotRented() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenThrow(new ValidationException("Отзыв может оставить только арендатор"));

        mvc.perform(post("/items/1/comment")
                        .header(HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CommentDto.builder().text("Отличная").build())))
                .andExpect(status().isBadRequest());
    }
}