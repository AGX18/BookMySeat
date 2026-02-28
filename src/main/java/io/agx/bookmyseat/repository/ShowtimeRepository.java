package io.agx.bookmyseat.repository;

import io.agx.bookmyseat.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long>, JpaSpecificationExecutor<Showtime> {

    Integer seatsPerRow = 8;

    char[] rows = {'A', 'B', 'C',  'D', 'E', 'F', 'G', 'H'};

    List<Showtime> findByMovieId(Long movieId);

    List<Showtime> findByScreenId(Long screenId);

    List<Showtime> findByMovieIdAndStartTimeAfter(Long movieId, LocalDateTime time);

    List<Showtime> findByScreenIdAndStartTimeBetween(Long screenId, LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT s FROM Showtime s
            JOIN FETCH s.movie
            JOIN FETCH s.screen
            WHERE s.movie.id = :movieId
            AND s.startTime > :time
            """)
    List<Showtime> findUpcomingShowtimesForMovie(@Param("movieId") Long movieId, @Param("time") LocalDateTime time);

    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
            FROM Showtime s
            WHERE s.screen.id = :screenId
            AND s.startTime < :endTime
            AND s.endTime > :startTime
            """)
    boolean existsOverlappingShowtime(@Param("screenId") Long screenId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);
}