package io.agx.bookmyseat.repository;

import io.agx.bookmyseat.BaseRepositoryTest;
import io.agx.bookmyseat.entity.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MovieRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    @BeforeEach
    void setUp() {
        movieRepository.save(Movie.builder()
                .title("The Dark Knight")
                .description("A superhero movie")
                .durationMins(152)
                .releaseDate(LocalDate.of(2008, 7, 18))
                .genre("ACTION")
                .build());

        movieRepository.save(Movie.builder()
                .title("Inception")
                .description("A mind bending thriller")
                .durationMins(148)
                .releaseDate(LocalDate.of(2010, 7, 16))
                .genre("THRILLER")
                .build());

        movieRepository.save(Movie.builder()
                .title("Wuthering Heights")
                .description("A romantic drama")
                .durationMins(123)
                .releaseDate(LocalDate.of(2026, 2, 14))
                .genre("ROMANCE")
                .build());
    }

    @Test
    void findByGenre_shouldReturnMoviesMatchingGenre() {
        List<Movie> result = movieRepository.findByGenre("ACTION");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("The Dark Knight");
    }

    @Test
    void findByGenre_shouldReturnEmpty_whenNoMoviesMatchGenre() {
        List<Movie> result = movieRepository.findByGenre("HORROR");

        assertThat(result).isEmpty();
    }

    @Test
    void findByTitleContainingIgnoreCase_shouldReturnMatchingMovies() {
        List<Movie> result = movieRepository.findByTitleContainingIgnoreCase("dark");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("The Dark Knight");
    }

    @Test
    void findByTitleContainingIgnoreCase_shouldBeCaseInsensitive() {
        List<Movie> resultLower = movieRepository.findByTitleContainingIgnoreCase("inception");
        List<Movie> resultUpper = movieRepository.findByTitleContainingIgnoreCase("INCEPTION");
        List<Movie> resultMixed = movieRepository.findByTitleContainingIgnoreCase("InCePtIoN");

        assertThat(resultLower).hasSize(1);
        assertThat(resultLower.getFirst().getTitle()).isEqualTo("Inception");
        assertThat(resultUpper).hasSize(1);
        assertThat(resultUpper.getFirst().getTitle()).isEqualTo("Inception");
        assertThat(resultMixed).hasSize(1);
        assertThat(resultMixed.getFirst().getTitle()).isEqualTo("Inception");
    }

    @Test
    void findByReleaseDateAfter_shouldReturnUpcomingMovies() {
        List<Movie> result = movieRepository.findByReleaseDateAfter(LocalDate.of(2026, 1, 1));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Wuthering Heights");
    }

    @Test
    void findByReleaseDateAfter_shouldReturnEmpty_whenNoUpcomingMovies() {
        List<Movie> result = movieRepository.findByReleaseDateAfter(LocalDate.of(2030, 1, 1));

        assertThat(result).isEmpty();
    }

    @Test
    void findByGenreAndReleaseDateAfter_shouldReturnMatchingMovies() {
        List<Movie> result = movieRepository.findByGenreAndReleaseDateAfter(
                "ROMANCE", LocalDate.of(2024, 1, 1));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Wuthering Heights");
    }

    @Test
    void findByGenreAndReleaseDateAfter_shouldReturnEmpty_whenNoMatch() {
        List<Movie> result = movieRepository.findByGenreAndReleaseDateAfter(
                "ACTION", LocalDate.of(2024, 1, 1));

        assertThat(result).isEmpty();
    }
}