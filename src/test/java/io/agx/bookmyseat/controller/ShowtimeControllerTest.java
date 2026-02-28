package io.agx.bookmyseat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.agx.bookmyseat.dto.request.CreateShowtimeRequest;
import io.agx.bookmyseat.dto.response.SeatResponse;
import io.agx.bookmyseat.dto.response.ShowtimeResponse;
import io.agx.bookmyseat.entity.SeatStatus;
import io.agx.bookmyseat.exception.ResourceNotFoundException;
import io.agx.bookmyseat.exception.ShowtimeOverlapException;
import io.agx.bookmyseat.service.SeatService;
import io.agx.bookmyseat.service.ShowtimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShowtimeController.class)
class ShowtimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Register JavaTimeModule so Jackson can serialize/deserialize LocalDateTime
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private ShowtimeService showtimeService;

    @MockitoBean
    private SeatService seatService;

    private CreateShowtimeRequest createRequest;
    private ShowtimeResponse showtimeResponse;
    private SeatResponse seatResponse;

    @BeforeEach
    void setUp() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);

        createRequest = CreateShowtimeRequest.builder()
                .movieId(1L)
                .screenId(10L)
                .startTime(startTime)
                .build();

        showtimeResponse = ShowtimeResponse.builder()
                .id(100L)
                .movieId(1L)
                .movieTitle("Inception")
                .screenId(10L)
                .screenName("IMAX Screen")
                .startTime(startTime)
                .build();

        seatResponse = SeatResponse.builder()
                .id(500L)
                .row('A')
                .number(1)
                .status(SeatStatus.AVAILABLE)
                .build();
    }

    // ==========================================
    // SHOWTIME TESTS
    // ==========================================

    @Test
    void createShowtime_shouldReturnCreatedStatusAndResponse() throws Exception {
        Mockito.when(showtimeService.createShowtime(any(CreateShowtimeRequest.class)))
                .thenReturn(showtimeResponse);

        mockMvc.perform(post("/api/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.movieTitle").value("Inception"));
    }

    @Test
    void createShowtime_shouldReturnBadRequest_whenValidationFails() throws Exception {
        // Simulating invalid request: Start time is in the past!
        CreateShowtimeRequest invalidRequest = CreateShowtimeRequest.builder()
                .movieId(1L)
                .screenId(10L)
                .startTime(LocalDateTime.now().minusDays(1)) // @Future should catch this
                .build();

        mockMvc.perform(post("/api/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        Mockito.verify(showtimeService, Mockito.never()).createShowtime(any());
    }

    @Test
    void getShowtimes_withoutParams_shouldReturnAllShowtimes() throws Exception {
        Mockito.when(showtimeService.getShowtimes(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(showtimeResponse));

        mockMvc.perform(get("/api/showtimes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    void getShowtimes_withParams_shouldPassParamsToService() throws Exception {
        LocalDate queryDate = LocalDate.of(2026, 3, 1);

        Mockito.when(showtimeService.getShowtimes(1L, 10L, 5L, queryDate))
                .thenReturn(List.of(showtimeResponse));

        mockMvc.perform(get("/api/showtimes")
                        .param("movieId", "1")
                        .param("screenId", "10")
                        .param("theaterId", "5")
                        .param("date", "2026-03-01") // Must match DateTimeFormat.ISO.DATE
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].movieTitle").value("Inception"));
    }

    @Test
    void getShowtimeById_shouldReturnOkStatusAndResponse() throws Exception {
        Mockito.when(showtimeService.getShowtimeById(100L)).thenReturn(showtimeResponse);

        mockMvc.perform(get("/api/showtimes/{id}", 100L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void deleteShowtime_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/showtimes/{id}", 100L))
                .andExpect(status().isNoContent());

        Mockito.verify(showtimeService, Mockito.times(1)).deleteShowtime(100L);
    }

    // ==========================================
    // NESTED SEAT TESTS
    // ==========================================

    @Test
    void getSeats_shouldReturnListOfAllSeats() throws Exception {
        Mockito.when(seatService.getSeatsByShowtime(100L))
                .thenReturn(List.of(seatResponse));

        mockMvc.perform(get("/api/showtimes/{id}/seats", 100L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(500))
                .andExpect(jsonPath("$[0].row").value("A"))
                .andExpect(jsonPath("$[0].number").value(1));
    }

    @Test
    void getAvailableSeats_shouldReturnListOfOnlyAvailableSeats() throws Exception {
        Mockito.when(seatService.getAvailableSeatsByShowtime(100L))
                .thenReturn(List.of(seatResponse));

        mockMvc.perform(get("/api/showtimes/{id}/seats/available", 100L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    void createShowtime_shouldReturnBadRequest_whenMissingRequiredFields() throws Exception {
        // Request missing movieId and screenId
        CreateShowtimeRequest invalidRequest = CreateShowtimeRequest.builder()
                .startTime(LocalDateTime.now().plusDays(1))
                .build();

        mockMvc.perform(post("/api/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Verify the service was protected from this bad data
        Mockito.verify(showtimeService, Mockito.never()).createShowtime(any());
    }

    @Test
    void getShowtimes_shouldReturnBadRequest_whenDateFormatIsInvalid() throws Exception {
        mockMvc.perform(get("/api/showtimes")
                        .param("date", "03/01/2026") // Invalid format (Not ISO)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Verify the service wasn't called with garbage data
        Mockito.verify(showtimeService, Mockito.never()).getShowtimes(any(), any(), any(), any());
    }

    @Test
    void getShowtimes_withOnlyDateParam_shouldPassDateAndNulls() throws Exception {
        LocalDate queryDate = LocalDate.of(2026, 3, 1);

        // Expecting nulls for the IDs, but a valid date
        Mockito.when(showtimeService.getShowtimes(isNull(), isNull(), isNull(), eq(queryDate)))
                .thenReturn(List.of(showtimeResponse));

        mockMvc.perform(get("/api/showtimes")
                        .param("date", "2026-03-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    void getShowtimeById_shouldReturnBadRequest_whenIdIsString() throws Exception {
        mockMvc.perform(get("/api/showtimes/abc") // "abc" cannot be parsed to Long
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getShowtimeById_shouldReturn404_whenShowtimeDoesNotExist() throws Exception {
        Mockito.when(showtimeService.getShowtimeById(999L))
                .thenThrow(new ResourceNotFoundException("Showtime", 999L));

        mockMvc.perform(get("/api/showtimes/{id}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteShowtime_shouldReturn404_whenShowtimeDoesNotExist() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException("Showtime", 999L))
                .when(showtimeService).deleteShowtime(999L);

        mockMvc.perform(delete("/api/showtimes/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void createShowtime_shouldReturn409_whenShowtimeOverlaps() throws Exception {
        Mockito.when(showtimeService.createShowtime(any(CreateShowtimeRequest.class)))
                .thenThrow(new ShowtimeOverlapException());

        mockMvc.perform(post("/api/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void getSeats_shouldReturn404_whenShowtimeDoesNotExist() throws Exception {
        Mockito.when(seatService.getSeatsByShowtime(999L))
                .thenThrow(new ResourceNotFoundException("Showtime", 999L));

        mockMvc.perform(get("/api/showtimes/{id}/seats", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAvailableSeats_shouldReturn404_whenShowtimeDoesNotExist() throws Exception {
        Mockito.when(seatService.getAvailableSeatsByShowtime(999L))
                .thenThrow(new ResourceNotFoundException("Showtime", 999L));

        mockMvc.perform(get("/api/showtimes/{id}/seats/available", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}