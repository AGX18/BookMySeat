package io.agx.bookmyseat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.agx.bookmyseat.dto.request.CreateBookingRequest;
import io.agx.bookmyseat.dto.response.BookingResponse;
import io.agx.bookmyseat.dto.response.SeatResponse;
import io.agx.bookmyseat.entity.BookingStatus;
import io.agx.bookmyseat.entity.SeatStatus;
import io.agx.bookmyseat.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private BookingService bookingService;

    private UUID userId;
    private UUID confirmationId;
    private CreateBookingRequest createRequest;
    private BookingResponse bookingResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        confirmationId = UUID.randomUUID();

        createRequest = CreateBookingRequest.builder()
                .userId(userId)
                .showtimeId(100L)
                .seatIds(List.of(1L, 2L))
                .build();

        SeatResponse seat1 = SeatResponse.builder().id(1L).row('A').number(1).status(SeatStatus.BOOKED).build();
        SeatResponse seat2 = SeatResponse.builder().id(2L).row('A').number(2).status(SeatStatus.BOOKED).build();

        bookingResponse = BookingResponse.builder()
                .confirmationId(confirmationId)
                .status(BookingStatus.CONFIRMED)
                .showtimeId(100L)
                .movieTitle("Interstellar")
                .theaterName("VOX Cinemas")
                .screenName("IMAX")
                .startTime(LocalDateTime.now().plusDays(2))
                .seats(List.of(seat1, seat2))
                .bookingTime(LocalDateTime.now())
                .build();
    }

    @Test
    void createBooking_shouldReturnCreatedStatusAndResponse() throws Exception {
        Mockito.when(bookingService.createBooking(any(CreateBookingRequest.class)))
                .thenReturn(bookingResponse);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.confirmationId").value(confirmationId.toString()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.seats").isArray())
                .andExpect(jsonPath("$.seats[0].number").value(1));
    }

    @Test
    void createBooking_shouldReturnBadRequest_whenMissingFields() throws Exception {
        // Missing the seatIds entirely
        CreateBookingRequest invalidRequest = CreateBookingRequest.builder()
                .userId(userId)
                .showtimeId(100L)
                .build();

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingService, Mockito.never()).createBooking(any());
    }

    @Test
    void getBookingByConfirmationId_shouldReturnOkStatusAndResponse() throws Exception {
        Mockito.when(bookingService.getBookingByConfirmationId(confirmationId))
                .thenReturn(bookingResponse);

        mockMvc.perform(get("/api/bookings/{confirmationId}", confirmationId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieTitle").value("Interstellar"))
                .andExpect(jsonPath("$.confirmationId").value(confirmationId.toString()));
    }

    @Test
    void cancelBooking_shouldReturnOkStatusAndUpdatedResponse() throws Exception {
        // Modify the mock response to simulate a cancelled state
        bookingResponse.setStatus(BookingStatus.CANCELLED);

        Mockito.when(bookingService.cancelBooking(confirmationId))
                .thenReturn(bookingResponse);

        mockMvc.perform(patch("/api/bookings/{confirmationId}/cancel", confirmationId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void getBookingsByUser_shouldReturnListOfBookings() throws Exception {
        Mockito.when(bookingService.getBookingsByUser(userId))
                .thenReturn(List.of(bookingResponse));

        mockMvc.perform(get("/api/bookings/user/{userId}", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].movieTitle").value("Interstellar"));
    }
}