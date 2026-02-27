package io.agx.bookmyseat.repository;

import io.agx.bookmyseat.BaseRepositoryTest;
import io.agx.bookmyseat.entity.Movie;
import io.agx.bookmyseat.entity.Screen;
import io.agx.bookmyseat.entity.Showtime;
import io.agx.bookmyseat.entity.Theater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShowtimeRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Movie inception;
    private Movie interstellar;
    private Screen screen1;
    private Screen screen2;

    private Theater voxAlmaza;


    private final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 3, 1, 14, 0); // 2:00 PM
    @BeforeEach
    void setUp() {
        showtimeRepository.deleteAll();

        // 1. Setup Theater (Because a Screen CANNOT exist without a Theater)
        Theater theater = entityManager.persistAndFlush(Theater.builder()
                .name("VOX Cinemas")
                .branch("Mall of Egypt")
                .city("Giza")
                .address("Al Wahat Road")
                .build());

        // 2. Setup Movies (Providing the required release_date, genre, and description)
        inception = entityManager.persistAndFlush(Movie.builder()
                .title("Inception")
                .description("A thief who steals corporate secrets through the use of dream-sharing technology.")
                .genre("Sci-Fi")
                .releaseDate(java.time.LocalDate.of(2010, 7, 16)) // Satisfies the Not-Null constraint!
                .durationMins(148)
                .build());

        interstellar = entityManager.persistAndFlush(Movie.builder()
                .title("Interstellar")
                .description("A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.")
                .genre("Sci-Fi")
                .releaseDate(java.time.LocalDate.of(2014, 11, 7)) // Satisfies the Not-Null constraint!
                .durationMins(169)
                .build());

        // 3. Setup Screens (Linked to the Theater)
        screen1 = entityManager.persistAndFlush(Screen.builder()
                .name("IMAX Screen 1")
                .theater(theater)
                .build());

        screen2 = entityManager.persistAndFlush(Screen.builder()
                .name("Standard Screen 2")
                .theater(theater)
                .build());

        // 4. Setup Showtimes (Exactly as they were)
        showtimeRepository.save(Showtime.builder()
                .movie(inception)
                .screen(screen1)
                .startTime(BASE_TIME)
                .endTime(BASE_TIME.plusMinutes(150))
                .build());

        showtimeRepository.save(Showtime.builder()
                .movie(inception)
                .screen(screen2)
                .startTime(BASE_TIME.plusHours(4))
                .endTime(BASE_TIME.plusHours(6).plusMinutes(30))
                .build());

        showtimeRepository.save(Showtime.builder()
                .movie(interstellar)
                .screen(screen1)
                .startTime(BASE_TIME.plusHours(6))
                .endTime(BASE_TIME.plusHours(9))
                .build());
    }
    @Test
    void findByMovieId_shouldReturnAllShowtimesForMovie() {
        List<Showtime> showtimes = showtimeRepository.findByMovieId(inception.getId());

        assertThat(showtimes).hasSize(2)
                .extracting(s -> s.getScreen().getName())
                .containsExactlyInAnyOrder("IMAX Screen 1", "Standard Screen 2");
    }

    @Test
    void findByScreenId_shouldReturnAllShowtimesForScreen() {
        List<Showtime> showtimes = showtimeRepository.findByScreenId(screen1.getId());

        assertThat(showtimes).hasSize(2)
                .extracting(s -> s.getMovie().getTitle())
                .containsExactlyInAnyOrder("Inception", "Interstellar");
    }

    @Test
    void findByMovieIdAndStartTimeAfter_shouldReturnOnlyFutureShowtimes() {
        // Query at 3:00 PM (After Showtime A has started, before Showtime B starts)
        LocalDateTime queryTime = BASE_TIME.plusHours(1);

        List<Showtime> upcoming = showtimeRepository.findByMovieIdAndStartTimeAfter(inception.getId(), queryTime);

        // Should only return Showtime B (6:00 PM)
        assertThat(upcoming).hasSize(1);
        assertThat(upcoming.getFirst().getScreen().getName()).isEqualTo("Standard Screen 2");
    }

    @Test
    void findByScreenIdAndStartTimeBetween_shouldReturnShowtimesInWindow() {
        // Window: 1:00 PM to 5:00 PM
        LocalDateTime startWindow = BASE_TIME.minusHours(1);
        LocalDateTime endWindow = BASE_TIME.plusHours(3);

        List<Showtime> showtimes = showtimeRepository.findByScreenIdAndStartTimeBetween(screen1.getId(), startWindow, endWindow);

        // Should only catch Showtime A (2:00 PM)
        assertThat(showtimes).hasSize(1);
        assertThat(showtimes.getFirst().getMovie().getTitle()).isEqualTo("Inception");
    }

    @Test
    void findUpcomingShowtimesForMovie_shouldFetchShowtimeWithMovieAndScreen() {
        LocalDateTime queryTime = BASE_TIME.minusHours(1); // 1:00 PM

        List<Showtime> showtimes = showtimeRepository.findUpcomingShowtimesForMovie(inception.getId(), queryTime);

        assertThat(showtimes).hasSize(2);

        // Verifying that the JOIN FETCH actually pulled the related entities
        // without throwing LazyInitializationException
        Showtime firstShowtime = showtimes.getFirst();
        assertThat(firstShowtime.getMovie().getTitle()).isEqualTo("Inception");
        assertThat(firstShowtime.getScreen().getName()).isNotNull();
    }

    // --- OVERLAP LOGIC TESTS ---
    // Target Showtime is exactly 2:00 PM to 4:30 PM on Screen 1

    @Test
    void existsOverlappingShowtime_shouldReturnTrue_whenCompletelyInside() {
        // Attempt: 3:00 PM to 4:00 PM (Fits entirely inside the existing showtime)
        boolean overlap = showtimeRepository.existsOverlappingShowtime(
                screen1.getId(), BASE_TIME.plusHours(1), BASE_TIME.plusHours(2));
        assertThat(overlap).isTrue();
    }

    @Test
    void existsOverlappingShowtime_shouldReturnTrue_whenOverlappingStart() {
        // Attempt: 1:00 PM to 3:00 PM (Overlaps the beginning)
        boolean overlap = showtimeRepository.existsOverlappingShowtime(
                screen1.getId(), BASE_TIME.minusHours(1), BASE_TIME.plusHours(1));
        assertThat(overlap).isTrue();
    }

    @Test
    void existsOverlappingShowtime_shouldReturnTrue_whenOverlappingEnd() {
        // Attempt: 4:00 PM to 5:00 PM (Overlaps the end)
        boolean overlap = showtimeRepository.existsOverlappingShowtime(
                screen1.getId(), BASE_TIME.plusHours(2), BASE_TIME.plusHours(3));
        assertThat(overlap).isTrue();
    }

    @Test
    void existsOverlappingShowtime_shouldReturnFalse_whenAdjacentBefore() {
        // Attempt: 12:00 PM to 2:00 PM (Ends exactly when target starts - no overlap)
        boolean overlap = showtimeRepository.existsOverlappingShowtime(
                screen1.getId(), BASE_TIME.minusHours(2), BASE_TIME);
        assertThat(overlap).isFalse();
    }

    @Test
    void existsOverlappingShowtime_shouldReturnFalse_whenAdjacentAfter() {
        // Attempt: 4:30 PM to 6:30 PM (Starts exactly when target ends - no overlap)
        boolean overlap = showtimeRepository.existsOverlappingShowtime(
                screen1.getId(), BASE_TIME.plusMinutes(150), BASE_TIME.plusMinutes(270));
        assertThat(overlap).isFalse();
    }
}