package io.agx.bookmyseat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.agx.bookmyseat.dto.request.CreateScreenRequest;
import io.agx.bookmyseat.dto.request.CreateTheaterRequest;
import io.agx.bookmyseat.dto.response.ScreenResponse;
import io.agx.bookmyseat.dto.response.TheaterResponse;
import io.agx.bookmyseat.service.ScreenService;
import io.agx.bookmyseat.service.TheaterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TheaterController.class)
class TheaterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private TheaterService theaterService;

    @MockitoBean
    private ScreenService screenService;

    private TheaterResponse theaterResponse;
    private CreateTheaterRequest createTheaterRequest;

    private ScreenResponse screenResponse;
    private CreateScreenRequest createScreenRequest;

    @BeforeEach
    void setUp() {
        theaterResponse = TheaterResponse.builder()
                .id(1L)
                .name("VOX Cinemas")
                .branch("Mall of Egypt")
                .city("Giza")
                .address("Al Wahat Road")
                .build();

        createTheaterRequest = CreateTheaterRequest.builder()
                .name("VOX Cinemas")
                .branch("Mall of Egypt")
                .city("Giza")
                .address("Al Wahat Road")
                .build();

        screenResponse = ScreenResponse.builder()
                .id(10L)
                .name("IMAX Screen 1")
                .build();

        createScreenRequest = CreateScreenRequest.builder()
                .name("IMAX Screen 1")
                .build();
    }

    // ==========================================
    // THEATER ENDPOINT TESTS
    // ==========================================

    @Test
    void createTheater_shouldReturnCreatedStatusAndResponse() throws Exception {
        Mockito.when(theaterService.createTheater(any(CreateTheaterRequest.class)))
                .thenReturn(theaterResponse);

        mockMvc.perform(post("/api/theaters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTheaterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("VOX Cinemas"));
    }

    @Test
    void getAllTheaters_withoutParams_shouldReturnListOfTheaters() throws Exception {
        // Mocking the service call where city and name are null
        Mockito.when(theaterService.getAllTheaters(isNull(), isNull()))
                .thenReturn(List.of(theaterResponse));

        mockMvc.perform(get("/api/theaters")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllTheaters_withParams_shouldPassParamsToService() throws Exception {
        // Mocking the service call expecting specific city and name parameters
        Mockito.when(theaterService.getAllTheaters("Giza", "VOX Cinemas"))
                .thenReturn(List.of(theaterResponse));

        mockMvc.perform(get("/api/theaters")
                        .param("city", "Giza")
                        .param("name", "VOX Cinemas")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].city").value("Giza"));
    }

    @Test
    void getTheaterById_shouldReturnOkStatusAndResponse() throws Exception {
        Mockito.when(theaterService.getTheaterById(1L)).thenReturn(theaterResponse);

        mockMvc.perform(get("/api/theaters/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.branch").value("Mall of Egypt"));
    }

    @Test
    void updateTheater_shouldReturnOkStatusAndUpdatedResponse() throws Exception {
        Mockito.when(theaterService.updateTheater(eq(1L), any(CreateTheaterRequest.class)))
                .thenReturn(theaterResponse);

        mockMvc.perform(put("/api/theaters/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTheaterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteTheater_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/theaters/{id}", 1L))
                .andExpect(status().isNoContent());

        Mockito.verify(theaterService, Mockito.times(1)).deleteTheater(1L);
    }

    // ==========================================
    // SCREEN ENDPOINT TESTS (Nested)
    // ==========================================

    @Test
    void addScreen_shouldReturnCreatedStatusAndScreenResponse() throws Exception {
        Mockito.when(screenService.createScreen(eq(1L), any(CreateScreenRequest.class)))
                .thenReturn(screenResponse);

        mockMvc.perform(post("/api/theaters/{id}/screens", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createScreenRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("IMAX Screen 1"));
    }

    @Test
    void getScreens_shouldReturnListOfScreensForTheater() throws Exception {
        Mockito.when(screenService.getScreensByTheater(1L)).thenReturn(List.of(screenResponse));

        mockMvc.perform(get("/api/theaters/{id}/screens", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void getScreenById_shouldReturnOkStatusAndScreenResponse() throws Exception {
        Mockito.when(screenService.getScreenById(1L, 10L)).thenReturn(screenResponse);

        mockMvc.perform(get("/api/theaters/{theaterId}/screens/{screenId}", 1L, 10L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("IMAX Screen 1"));
    }

    @Test
    void updateScreen_shouldReturnOkStatusAndUpdatedScreen() throws Exception {
        Mockito.when(screenService.updateScreen(eq(1L), eq(10L), any(CreateScreenRequest.class)))
                .thenReturn(screenResponse);

        mockMvc.perform(put("/api/theaters/{theaterId}/screens/{screenId}", 1L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createScreenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void deleteScreen_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/theaters/{theaterId}/screens/{screenId}", 1L, 10L))
                .andExpect(status().isNoContent());

        Mockito.verify(screenService, Mockito.times(1)).deleteScreen(1L, 10L);
    }

    @Test
    void getAllTheaters_withOnlyCityParam_shouldPassCityAndNullName() throws Exception {
        // Mocking the service call expecting city="Giza" and name=null
        Mockito.when(theaterService.getAllTheaters("Giza", null))
                .thenReturn(List.of(theaterResponse));

        mockMvc.perform(get("/api/theaters")
                        .param("city", "Giza")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].city").value("Giza"));
    }

    @Test
    void getAllTheaters_withOnlyNameParam_shouldPassNameAndNullCity() throws Exception {
        // Mocking the service call expecting city=null and name="VOX Cinemas"
        Mockito.when(theaterService.getAllTheaters(null, "VOX Cinemas"))
                .thenReturn(List.of(theaterResponse));

        mockMvc.perform(get("/api/theaters")
                        .param("name", "VOX Cinemas")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("VOX Cinemas"));
    }
}