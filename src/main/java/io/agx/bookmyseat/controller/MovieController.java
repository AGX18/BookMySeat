package io.agx.bookmyseat.controller;

import io.agx.bookmyseat.dto.request.CreateMovieRequest;
import io.agx.bookmyseat.dto.response.MovieResponse;
import io.agx.bookmyseat.service.MovieService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getMovies(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) LocalDate releasedAfter) {
        return ResponseEntity.ok(movieService.getMovies(genre, title, releasedAfter));
    }


    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody CreateMovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.createMovie(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable Long id, @Valid @RequestBody CreateMovieRequest request) {
        return ResponseEntity.ok(movieService.updateMovie(id, request));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
