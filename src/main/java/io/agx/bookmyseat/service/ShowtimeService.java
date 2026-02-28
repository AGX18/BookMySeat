package io.agx.bookmyseat.service;

import io.agx.bookmyseat.dto.request.CreateShowtimeRequest;
import io.agx.bookmyseat.dto.response.SeatResponse;
import io.agx.bookmyseat.dto.response.ShowtimeResponse;
import io.agx.bookmyseat.entity.Movie;
import io.agx.bookmyseat.entity.Screen;
import io.agx.bookmyseat.entity.Showtime;
import io.agx.bookmyseat.exception.ResourceNotFoundException;
import io.agx.bookmyseat.repository.MovieRepository;
import io.agx.bookmyseat.repository.ScreenRepository;
import io.agx.bookmyseat.repository.ShowtimeRepository;
import io.agx.bookmyseat.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    public ShowtimeResponse getShowtimeById(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", id));
        return ShowtimeResponse.from(showtime);
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
    public ShowtimeResponse createShowtime(CreateShowtimeRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", request.getMovieId()));

        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen", request.getScreenId()));

        LocalDateTime endTime = request.getStartTime().plusMinutes(movie.getDurationMins());


        if (showtimeRepository.existsOverlappingShowtime(request.getScreenId(), request.getStartTime(), endTime)) {
            throw new IllegalArgumentException("Showtime overlaps with an existing showtime on this screen");
        }

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .screen(screen)
                .startTime(request.getStartTime())
                .endTime(endTime)
                .build();

        showtime = showtimeRepository.save(showtime);

        seatService.createSeatsForShowtime(showtime.getId(), ShowtimeRepository.rows, ShowtimeRepository.seatsPerRow);

        return ShowtimeResponse.from(showtime);
    }

    public List<ShowtimeResponse> getShowtimes(Long movieId, Long screenId, Long theaterId, LocalDate date) {
        return showtimeRepository.findAll().stream()
                .filter(s -> movieId == null || s.getMovie().getId().equals(movieId))
                .filter(s -> screenId == null || s.getScreen().getId().equals(screenId))
                .filter(s -> theaterId == null || s.getScreen().getTheater().getId().equals(theaterId))
                .filter(s -> date == null || s.getStartTime().toLocalDate().equals(date))
                .map(ShowtimeResponse::from)
                .toList();
    }

    @Transactional
    public void deleteShowtime(Long id) {
        if (!showtimeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Showtime", id);
        }
        showtimeRepository.deleteById(id);
    }
}