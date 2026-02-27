package io.agx.bookmyseat.repository;

import io.agx.bookmyseat.BaseRepositoryTest;
import io.agx.bookmyseat.entity.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BookingRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EntityManager entityManager;

    private User user;
    private Showtime showtime;
    private Booking confirmedBooking;
    private Booking cancelledBooking;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();

        // 1. Build the User (Assuming User ID is a UUID based on your service logic)
        user = User.builder()
                .name("John Doe")
                .email("john.bookingtest@example.com")
                .passwordHash("hashed")
                .build();
        entityManager.persist(user);

        // 2. Build Theater, Screen, and Movie
        Theater theater = Theater.builder()
                .name("VOX Cinemas")
                .branch("Mall of Egypt")
                .city("Giza")
                .address("Al Wahat Road")
                .build();
        entityManager.persist(theater);

        Screen screen = Screen.builder()
                .name("IMAX Screen")
                .theater(theater)
                .build();
        entityManager.persist(screen);

        Movie movie = Movie.builder()
                .title("Dune: Part Two")
                .description("Paul Atreides unites with Chani.")
                .genre("Sci-Fi")
                .releaseDate(LocalDate.of(2024, 3, 1))
                .durationMins(166)
                .build();
        entityManager.persist(movie);

        // 3. Build Showtime
        showtime = Showtime.builder()
                .movie(movie)
                .screen(screen)
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(3))
                .build();
        entityManager.persist(showtime);

        // 4. Build Seats
        Seat seat1 = Seat.builder().showtime(showtime).row('A').number(1).status(SeatStatus.BOOKED).build();
        Seat seat2 = Seat.builder().showtime(showtime).row('A').number(2).status(SeatStatus.BOOKED).build();
        Seat seat3 = Seat.builder().showtime(showtime).row('A').number(3).status(SeatStatus.AVAILABLE).build(); // Cancelled seat

        entityManager.persist(seat1);
        entityManager.persist(seat2);
        entityManager.persist(seat3);

        // 5. Build Bookings
        confirmedBooking = Booking.builder()
                .user(user)
                .showtime(showtime)
                .seats(Set.of(seat1, seat2))
                .status(BookingStatus.CONFIRMED)
                .build();
        entityManager.persist(confirmedBooking);

        cancelledBooking = Booking.builder()
                .user(user)
                .showtime(showtime)
                .seats(Set.of(seat3))
                .status(BookingStatus.CANCELLED)
                .build();
        entityManager.persist(cancelledBooking);


        // Flush immediately to save all to DB
        entityManager.flush();
    }

    @Test
    void findByUserId_shouldReturnAllBookingsForUser() {
        List<Booking> bookings = bookingRepository.findByUserId(user.getId());

        assertThat(bookings).hasSize(2)
                .extracting(Booking::getStatus)
                .containsExactlyInAnyOrder(BookingStatus.CONFIRMED, BookingStatus.CANCELLED);
    }

    @Test
    void findByUserIdAndStatus_shouldReturnFilteredBookings() {
        List<Booking> activeBookings = bookingRepository.findByUserIdAndStatus(user.getId(), BookingStatus.CONFIRMED);

        assertThat(activeBookings).hasSize(1);
        assertThat(activeBookings.get(0).getId()).isEqualTo(confirmedBooking.getId());
    }

    @Test
    void findByConfirmationId_shouldReturnBooking() {
        Optional<Booking> booking = bookingRepository.findByConfirmationId(confirmedBooking.getConfirmationId());

        assertThat(booking).isPresent();
        assertThat(booking.get().getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void findByShowtimeId_shouldReturnBookingsForShowtime() {
        List<Booking> bookings = bookingRepository.findByShowtimeId(showtime.getId());

        assertThat(bookings).hasSize(2);
    }

    @Test
    void findByConfirmationIdWithDetails_shouldFetchAssociations() {
        // IMPORTANT: Clear the cache so Hibernate is forced to run the custom JOIN FETCH query
        entityManager.clear();

        Optional<Booking> bookingOpt = bookingRepository.findByConfirmationIdWithDetails(confirmedBooking.getConfirmationId());

        assertThat(bookingOpt).isPresent();
        Booking booking = bookingOpt.get();

        // If JOIN FETCH failed, these would throw LazyInitializationException
        // because the EntityManager is cleared.
        assertThat(booking.getUser().getName()).isEqualTo("John Doe");
        assertThat(booking.getShowtime().getMovie().getTitle()).isEqualTo("Dune: Part Two");
        assertThat(booking.getSeats()).hasSize(2);
    }

    @Test
    void findByUserIdWithDetails_shouldFetchAssociations() {
        entityManager.clear();

        List<Booking> bookings = bookingRepository.findByUserIdWithDetails(user.getId());

        assertThat(bookings).hasSize(2);

        Booking active = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .findFirst()
                .orElseThrow();

        assertThat(active.getShowtime().getScreen().getName()).isEqualTo("IMAX Screen");
        assertThat(active.getSeats()).isNotEmpty();
    }
}