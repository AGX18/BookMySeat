package io.agx.bookmyseat.service;

import io.agx.bookmyseat.dto.response.SeatResponse;
import io.agx.bookmyseat.entity.Seat;
import io.agx.bookmyseat.entity.SeatStatus;
import io.agx.bookmyseat.entity.Showtime;
import io.agx.bookmyseat.exception.ResourceNotFoundException;
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

    public List<SeatResponse> getSeatsByShowtime(Long showtimeId) {
        if (!showtimeRepository.existsById(showtimeId)) {
            throw new ResourceNotFoundException("Showtime", showtimeId);
        }
        return seatRepository.findByShowtimeId(showtimeId)
                .stream()
                .map(SeatResponse::from)
                .toList();
    }

    public List<SeatResponse> getAvailableSeatsByShowtime(Long showtimeId) {
        if (!showtimeRepository.existsById(showtimeId)) {
            throw new ResourceNotFoundException("Showtime", showtimeId);
        }
        return seatRepository.findByShowtimeIdAndStatus(showtimeId, SeatStatus.AVAILABLE)
                .stream()
                .map(SeatResponse::from)
                .toList();
    }

    public SeatResponse getSeatById(Long id) {
        if (!seatRepository.existsById(id)) {
            throw new ResourceNotFoundException("Seat", id);
        }
        Seat seat = seatRepository.findById(id).orElseThrow(
                ()  -> new ResourceNotFoundException("Seat", id)
        );
        return SeatResponse.from(seat);
    }

    @Transactional
    public void createSeatsForShowtime(Long showtimeId, char[] rows, int seatsPerRow) {
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

        seatRepository.saveAll(seats);
    }

    @Transactional
    public void updateSeatStatus(Long seatId, SeatStatus status) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + seatId));
        seat.setStatus(status);
        seatRepository.save(seat);
    }
}