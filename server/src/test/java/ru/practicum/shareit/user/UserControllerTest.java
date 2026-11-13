package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.DuplicatedEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private UserService userService;

    private final UserDto dasha = UserDto.builder().id(1L).name("Даша").email("dasha@mail.ru").build();

    @Test
    void createShouldReturnUser() throws Exception {
        when(userService.create(any(UserDto.class))).thenReturn(dasha);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                UserDto.builder().name("Даша").email("dasha@mail.ru").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Даша"))
                .andExpect(jsonPath("$.email").value("dasha@mail.ru"));
    }

    @Test
    void createShouldReturn400WhenEmailInvalid() throws Exception {
        // сервис даже не вызывается: запрос отсекает валидация
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                UserDto.builder().name("Даша").email("не-email").build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createShouldReturn409WhenEmailTaken() throws Exception {
        when(userService.create(any(UserDto.class)))
                .thenThrow(new DuplicatedEmailException("Этот email уже используется"));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                UserDto.builder().name("Даша").email("dasha@mail.ru").build())))
                .andExpect(status().isConflict());
    }

    @Test
    void updateShouldReturnUpdatedUser() throws Exception {
        UserDto updated = UserDto.builder().id(1L).name("Дарья").email("dasha@mail.ru").build();
        when(userService.update(anyLong(), any(UserDto.class))).thenReturn(updated);

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(UserDto.builder().name("Дарья").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Дарья"));
    }

    @Test
    void findByIdShouldReturnUser() throws Exception {
        when(userService.findById(1L)).thenReturn(dasha);

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void findByIdShouldReturn404WhenNotFound() throws Exception {
        when(userService.findById(999L)).thenThrow(new NotFoundException("Пользователь не найден"));

        mvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAllShouldReturnList() throws Exception {
        UserDto masha = UserDto.builder().id(2L).name("Маша").email("masha@mail.ru").build();
        when(userService.findAll()).thenReturn(List.of(dasha, masha));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].name").value("Маша"));
    }

    @Test
    void deleteShouldCallService() throws Exception {
        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
        
        verify(userService).delete(1L);
    }
}