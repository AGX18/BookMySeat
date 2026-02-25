package io.agx.bookmyseat.repository;

import io.agx.bookmyseat.entity.Booking;
import io.agx.bookmyseat.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(UUID userId);

    List<Booking> findByUserIdAndStatus(UUID userId, BookingStatus status);

    Optional<Booking> findByConfirmationId(UUID confirmationId);

    List<Booking> findByShowtimeId(Long showtimeId);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.user
            JOIN FETCH b.showtime
            JOIN FETCH b.seats
            WHERE b.confirmationId = :confirmationId
            """)
    Optional<Booking> findByConfirmationIdWithDetails(@Param("confirmationId") UUID confirmationId);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.showtime
            JOIN FETCH b.seats
            WHERE b.user.id = :userId
            """)
    List<Booking> findByUserIdWithDetails(@Param("userId") UUID userId);
}