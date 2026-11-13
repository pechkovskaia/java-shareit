package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemRequestService itemRequestService;

    private final ItemRequestDto request = ItemRequestDto.builder()
            .id(1L).description("Нужна палатка").created(LocalDateTime.now()).items(List.of()).build();

    @Test
    void createShouldReturnRequest() throws Exception {
        when(itemRequestService.create(eq(1L), any(ItemRequestDto.class))).thenReturn(request);

        mvc.perform(post("/requests")
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                ItemRequestDto.builder().description("Нужна палатка").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна палатка"));
    }

    @Test
    void createShouldReturn400WhenDescriptionBlank() throws Exception {
        mvc.perform(post("/requests")
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(ItemRequestDto.builder().description("").build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findOwnShouldReturnList() throws Exception {
        when(itemRequestService.findOwn(1L)).thenReturn(List.of(request));

        mvc.perform(get("/requests").header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void findAllOthersShouldReturnList() throws Exception {
        when(itemRequestService.findAllOthers(2L)).thenReturn(List.of(request));

        mvc.perform(get("/requests/all").header(HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Нужна палатка"));
    }

    @Test
    void findByIdShouldReturnRequest() throws Exception {
        when(itemRequestService.findById(2L, 1L)).thenReturn(request);

        mvc.perform(get("/requests/1").header(HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void unexpectedErrorShouldReturn500() throws Exception {
        when(itemRequestService.findById(2L, 1L)).thenThrow(new RuntimeException("Что-то сломалось"));

        mvc.perform(get("/requests/1").header(HEADER, 2))
                .andExpect(status().isInternalServerError());
    }
}