package io.agx.bookmyseat.service;

import io.agx.bookmyseat.entity.Movie;
import io.agx.bookmyseat.repository.MovieRepository;
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

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
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

    public Movie createMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    @Transactional
    public Movie updateMovie(Long id, Movie updated) {
        Movie existing = movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found: " + id));

        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setDurationMins(updated.getDurationMins());
        existing.setReleaseDate(updated.getReleaseDate());
        existing.setGenre(updated.getGenre());

        return movieRepository.save(existing);
    }

    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new IllegalArgumentException("Movie not found: " + id);
        }
        movieRepository.deleteById(id);
    }
}