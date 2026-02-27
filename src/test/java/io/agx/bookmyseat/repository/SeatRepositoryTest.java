package io.agx.bookmyseat.repository;

import io.agx.bookmyseat.BaseRepositoryTest;
import io.agx.bookmyseat.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeatRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Showtime showtime;
    private Seat seatA1;
    private Seat seatA2;
    private Seat seatB1;

    @BeforeEach
    void setUp() {
        seatRepository.deleteAll();

        // 1. Build the dependency chain
        Theater theater = entityManager.persistAndFlush(Theater.builder()
                .name("VOX Cinemas")
                .branch("Mall of Egypt")
                .city("Giza")
                .address("Al Wahat Road")
                .build());

        Movie movie = entityManager.persistAndFlush(Movie.builder()
                .title("Dune: Part Two")
                .description("Paul Atreides unites with Chani and the Fremen.")
                .genre("Sci-Fi")
                .releaseDate(LocalDate.of(2024, 3, 1))
                .durationMins(166)
                .build());

        Screen screen = entityManager.persistAndFlush(Screen.builder()
                .name("IMAX Screen")
                .theater(theater)
                .build());

        showtime = entityManager.persistAndFlush(Showtime.builder()
                .movie(movie)
                .screen(screen)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(3))
                .build());

        // 2. Setup Seats
        seatA1 = entityManager.persistAndFlush(Seat.builder()
                .showtime(showtime)
                .row('A')
                .number(1)
                .status(SeatStatus.AVAILABLE)
                .build());

        seatA2 = entityManager.persistAndFlush(Seat.builder()
                .showtime(showtime)
                .row('A')
                .number(2)
                .status(SeatStatus.BOOKED) // Purposely setting this to booked
                .build());

        seatB1 = entityManager.persistAndFlush(Seat.builder()
                .showtime(showtime)
                .row('B')
                .number(1)
                .status(SeatStatus.AVAILABLE)
                .build());
    }

    @Test
    void findByShowtimeId_shouldReturnAllSeatsForShowtime() {
        List<Seat> seats = seatRepository.findByShowtimeId(showtime.getId());

        assertThat(seats).hasSize(3)
                .extracting(Seat::getRow)
                .containsExactlyInAnyOrder('A', 'A', 'B');
    }

    @Test
    void findByShowtimeIdAndStatus_shouldReturnOnlyMatchingStatus() {
        List<Seat> availableSeats = seatRepository.findByShowtimeIdAndStatus(showtime.getId(), SeatStatus.AVAILABLE);
        List<Seat> bookedSeats = seatRepository.findByShowtimeIdAndStatus(showtime.getId(), SeatStatus.BOOKED);

        assertThat(availableSeats).hasSize(2).contains(seatA1, seatB1);
        assertThat(bookedSeats).hasSize(1).contains(seatA2);
    }

    @Test
    void findByShowtimeIdAndRowAndNumber_shouldReturnExactSeat() {
        Optional<Seat> seat = seatRepository.findByShowtimeIdAndRowAndNumber(showtime.getId(), 'A', 2);

        assertThat(seat).isPresent();
        assertThat(seat.get().getStatus()).isEqualTo(SeatStatus.BOOKED);
    }

    @Test
    void findAvailableSeatsByIds_shouldReturnOnlyAvailableSeatsInList() {
        // Requesting IDs for A1 (Available) and A2 (Booked)
        List<Long> requestedSeatIds = List.of(seatA1.getId(), seatA2.getId());

        List<Seat> availableSeats = seatRepository.findAvailableSeatsByIds(showtime.getId(), requestedSeatIds);

        // Should ONLY return A1, filtering out the booked A2
        assertThat(availableSeats).hasSize(1);
        assertThat(availableSeats.getFirst().getId()).isEqualTo(seatA1.getId());
    }

    @Test
    void updateStatusForSeats_shouldBulkUpdateStatus() {
        List<Long> seatsToBook = List.of(seatA1.getId(), seatB1.getId());

        int updatedCount = seatRepository.updateStatusForSeats(seatsToBook, SeatStatus.BOOKED);

        assertThat(updatedCount).isEqualTo(2);

        // CLEAR the cache to force Hibernate to read the updated rows from the DB
        entityManager.clear();

        Seat updatedA1 = seatRepository.findById(seatA1.getId()).orElseThrow();
        Seat updatedB1 = seatRepository.findById(seatB1.getId()).orElseThrow();

        assertThat(updatedA1.getStatus()).isEqualTo(SeatStatus.BOOKED);
        assertThat(updatedB1.getStatus()).isEqualTo(SeatStatus.BOOKED);
    }

    @Test
    void save_shouldNotAllowDuplicateSeatInSameShowtime() {
        assertThatThrownBy(() -> seatRepository.saveAndFlush(Seat.builder()
                .showtime(showtime)
                .row('A')    // Same Row
                .number(1)   // Same Number
                .status(SeatStatus.AVAILABLE)
                .build())).isInstanceOf(DataIntegrityViolationException.class);
    }
}