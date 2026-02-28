package io.agx.bookmyseat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.agx.bookmyseat.dto.request.CreateMovieRequest;
import io.agx.bookmyseat.dto.response.MovieResponse;
import io.agx.bookmyseat.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Manually instantiating and adding the time module to handle LocalDate serialization cleanly
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private MovieService movieService;

    private MovieResponse movieResponse;
    private CreateMovieRequest createRequest;

    @BeforeEach
    void setUp() {
        movieResponse = MovieResponse.builder()
                .id(1L)
                .title("Dune: Part Two")
                .genre("Sci-Fi")
                .releaseDate(LocalDate.of(2024, 3, 1))
                .durationMins(166)
                .build();

        createRequest = CreateMovieRequest.builder()
                .title("Dune: Part Two")
                .genre("Sci-Fi")
                .releaseDate(LocalDate.of(2024, 3, 1))
                .durationMins(166)
                .build();
    }

    @Test
    void getMovies_shouldReturnListOfMovies_withOrWithoutParams() throws Exception {
        Mockito.when(movieService.getMovies("Sci-Fi", null, LocalDate.of(2024, 1, 1)))
                .thenReturn(List.of(movieResponse));

        // Testing the endpoint with RequestParams
        mockMvc.perform(get("/api/movies")
                        .param("genre", "Sci-Fi")
                        .param("releasedAfter", "2024-01-01") // Spring converts this string to LocalDate automatically
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Dune: Part Two"));
    }

    @Test
    void createMovie_shouldReturnCreatedStatusAndMovieResponse() throws Exception {
        Mockito.when(movieService.createMovie(any(CreateMovieRequest.class)))
                .thenReturn(movieResponse);

        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Dune: Part Two"));
    }

    @Test
    void createMovie_shouldReturnBadRequest_whenValidationFails() throws Exception {
        // Simulating an invalid request (e.g., missing title)
        CreateMovieRequest invalidRequest = CreateMovieRequest.builder()
                .genre("Sci-Fi")
                .build();

        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        Mockito.verify(movieService, Mockito.never()).createMovie(any());
    }

    @Test
    void getMovieById_shouldReturnOkStatusAndMovieResponse() throws Exception {
        Mockito.when(movieService.getMovieById(1L)).thenReturn(movieResponse);

        mockMvc.perform(get("/api/movies/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Dune: Part Two"));
    }

    @Test
    void updateMovie_shouldReturnOkStatusAndUpdatedMovieResponse() throws Exception {
        Mockito.when(movieService.updateMovie(eq(1L), any(CreateMovieRequest.class)))
                .thenReturn(movieResponse);

        mockMvc.perform(put("/api/movies/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Dune: Part Two"));
    }

    @Test
    void deleteMovie_shouldReturnNoContentStatus() throws Exception {
        // For void methods, we don't need a Mockito.when(), just the perform action
        mockMvc.perform(delete("/api/movies/{id}", 1L))
                .andExpect(status().isNoContent());

        // Verify the service was actually called with the correct ID
        Mockito.verify(movieService, Mockito.times(1)).deleteMovie(1L);
    }
}