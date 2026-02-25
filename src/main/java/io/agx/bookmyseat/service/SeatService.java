package io.agx.bookmyseat.service;

import io.agx.bookmyseat.entity.Seat;
import io.agx.bookmyseat.entity.SeatStatus;
import io.agx.bookmyseat.entity.Showtime;
import io.agx.bookmyseat.repository.SeatRepository;
import io.agx.bookmyseat.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;

    public List<Seat> getSeatsByShowtime(Long showtimeId) {
        return seatRepository.findByShowtimeId(showtimeId);
    }

    public List<Seat> getAvailableSeatsByShowtime(Long showtimeId) {
        return seatRepository.findByShowtimeIdAndStatus(showtimeId, SeatStatus.AVAILABLE);
    }

    public Optional<Seat> getSeatById(Long id) {
        return seatRepository.findById(id);
    }

    @Transactional
    public List<Seat> createSeatsForShowtime(Long showtimeId, char[] rows, int seatsPerRow) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found: " + showtimeId));

        List<Seat> seats = new ArrayList<>();
        for (char row : rows) {
            for (int number = 1; number <= seatsPerRow; number++) {
                Seat seat = Seat.builder()
                        .showtime(showtime)
                        .row(row)
                        .number(number)
                        .status(SeatStatus.AVAILABLE)
                        .build();
                seats.add(seat);
            }
        }

        return seatRepository.saveAll(seats);
    }

    @Transactional
    public void updateSeatStatus(Long seatId, SeatStatus status) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + seatId));
        seat.setStatus(status);
        seatRepository.save(seat);
    }
}