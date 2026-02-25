package io.agx.bookmyseat.repository;

import io.agx.bookmyseat.entity.Seat;
import io.agx.bookmyseat.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByShowtimeId(Long showtimeId);

    List<Seat> findByShowtimeIdAndStatus(Long showtimeId, SeatStatus status);

    Optional<Seat> findByShowtimeIdAndRowAndNumber(Long showtimeId, Character row, Integer number);

    @Query("""
            SELECT s FROM Seat s
            WHERE s.showtime.id = :showtimeId
            AND s.id IN :seatIds
            AND s.status = 'AVAILABLE'
            """)
    List<Seat> findAvailableSeatsByIds(@Param("showtimeId") Long showtimeId,
                                       @Param("seatIds") List<Long> seatIds);

    @Modifying
    @Query("""
            UPDATE Seat s SET s.status = :status
            WHERE s.id IN :seatIds
            """)
    int updateStatusForSeats(@Param("seatIds") List<Long> seatIds,
                             @Param("status") SeatStatus status);
}