package io.agx.bookmyseat.service;

import io.agx.bookmyseat.dto.request.CreateMovieRequest;
import io.agx.bookmyseat.dto.response.MovieResponse;
import io.agx.bookmyseat.entity.Movie;
import io.agx.bookmyseat.exception.ResourceNotFoundException;
import io.agx.bookmyseat.repository.MovieRepository;
import io.agx.bookmyseat.specification.MovieSpecification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieResponse createMovie(CreateMovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .durationMins(request.getDurationMins())
                .releaseDate(request.getReleaseDate())
                .genre(request.getGenre())
                .build();
        return MovieResponse.from(movieRepository.save(movie));
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", id));
        return MovieResponse.from(movie);
    }

    public List<Movie> getMoviesByGenre(String genre) {
        return movieRepository.findByGenre(genre);
    }

    public List<Movie> searchMoviesByTitle(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Movie> getUpcomingMovies() {
        return movieRepository.findByReleaseDateAfter(LocalDate.now());
    }


    @Transactional
    public MovieResponse updateMovie(Long id, CreateMovieRequest updated) {
        Movie existing = movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found: " + id));

        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setDurationMins(updated.getDurationMins());
        existing.setReleaseDate(updated.getReleaseDate());
        existing.setGenre(updated.getGenre());

        return MovieResponse.from(movieRepository.save(existing));
    }

    public List<MovieResponse> getMovies(String genre, String title, LocalDate releasedAfter) {
        return movieRepository.findAll(MovieSpecification.filter(genre, title, releasedAfter))
                .stream()
                .map(MovieResponse::from)
                .toList();
    }

    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new IllegalArgumentException("Movie not found: " + id);
        }
        movieRepository.deleteById(id);
    }


}