package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private BookingService bookingService;

    private final LocalDateTime start = LocalDateTime.now().plusDays(1);
    private final LocalDateTime end = LocalDateTime.now().plusDays(2);
    private final BookingDto booking = BookingDto.builder()
            .id(1L).start(start).end(end).status(BookingStatus.WAITING).build();

    @Test
    void createShouldReturnBooking() throws Exception {
        NewBookingDto request = new NewBookingDto();
        request.setItemId(1L);
        request.setStart(start);
        request.setEnd(end);
        when(bookingService.create(eq(2L), any(NewBookingDto.class))).thenReturn(booking);

        mvc.perform(post("/bookings")
                        .header(HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approveShouldReturnBooking() throws Exception {
        BookingDto approved = BookingDto.builder()
                .id(1L).start(start).end(end).status(BookingStatus.APPROVED).build();
        when(bookingService.approve(1L, 1L, true)).thenReturn(approved);

        mvc.perform(patch("/bookings/1").param("approved", "true").header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void findByIdShouldReturnBooking() throws Exception {
        when(bookingService.findById(2L, 1L)).thenReturn(booking);

        mvc.perform(get("/bookings/1").header(HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void findAllByBookerShouldUseAllByDefault() throws Exception {
        when(bookingService.findAllByBooker(2L, BookingState.ALL)).thenReturn(List.of(booking));

        // state не передаём — должен подставиться ALL
        mvc.perform(get("/bookings").header(HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(bookingService).findAllByBooker(2L, BookingState.ALL);
    }

    @Test
    void findAllByOwnerShouldPassState() throws Exception {
        when(bookingService.findAllByOwner(1L, BookingState.FUTURE)).thenReturn(List.of(booking));

        mvc.perform(get("/bookings/owner").param("state", "FUTURE").header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void unknownStateShouldReturn400() throws Exception {
        mvc.perform(get("/bookings").param("state", "HELLO").header(HEADER, 2))
                .andExpect(status().isBadRequest());
    }
}