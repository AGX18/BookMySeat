package io.agx.bookmyseat.service;

import io.agx.bookmyseat.entity.Movie;
import io.agx.bookmyseat.entity.Screen;
import io.agx.bookmyseat.entity.Showtime;
import io.agx.bookmyseat.repository.MovieRepository;
import io.agx.bookmyseat.repository.ScreenRepository;
import io.agx.bookmyseat.repository.ShowtimeRepository;
import io.agx.bookmyseat.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final SeatService  seatService;

    public Optional<Showtime> getShowtimeById(Long id) {
        return showtimeRepository.findById(id);
    }

    public List<Showtime> getShowtimesByMovie(Long movieId) {
        return showtimeRepository.findByMovieId(movieId);
    }

    public List<Showtime> getShowtimesByScreen(Long screenId) {
        return showtimeRepository.findByScreenId(screenId);
    }

    public List<Showtime> getUpcomingShowtimesForMovie(Long movieId) {
        return showtimeRepository.findUpcomingShowtimesForMovie(movieId, LocalDateTime.now());
    }

    @Transactional
    public Showtime createShowtime(Long movieId, Long screenId, LocalDateTime startTime) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found: " + movieId));

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new IllegalArgumentException("Screen not found: " + screenId));

        LocalDateTime endTime = startTime.plusMinutes(movie.getDurationMins());

        if (showtimeRepository.existsOverlappingShowtime(screenId, startTime, endTime)) {
            throw new IllegalStateException("Screen already has a showtime during this period");
        }

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .screen(screen)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        seatService.createSeatsForShowtime(showtime.getId(), ShowtimeRepository.rows, ShowtimeRepository.seatsPerRow);


        return showtimeRepository.save(showtime);
    }

    @Transactional
    public void deleteShowtime(Long id) {
        if (!showtimeRepository.existsById(id)) {
            throw new IllegalArgumentException("Showtime not found: " + id);
        }
        showtimeRepository.deleteById(id);
    }
}