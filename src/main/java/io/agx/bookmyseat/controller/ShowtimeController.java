package io.agx.bookmyseat.controller;

import io.agx.bookmyseat.dto.request.CreateShowtimeRequest;
import io.agx.bookmyseat.dto.response.SeatResponse;
import io.agx.bookmyseat.dto.response.ShowtimeResponse;
import io.agx.bookmyseat.service.SeatService;
import io.agx.bookmyseat.service.ShowtimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;
    private final SeatService seatService;

    @PostMapping
    public ResponseEntity<ShowtimeResponse> createShowtime(@Valid @RequestBody CreateShowtimeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(showtimeService.createShowtime(request));
    }

    @GetMapping
    public ResponseEntity<List<ShowtimeResponse>> getShowtimes(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long screenId,
            @RequestParam(required = false) Long theaterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showtimeService.getShowtimes(movieId, screenId, theaterId, date));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowtimeResponse> getShowtimeById(@PathVariable Long id) {
        return ResponseEntity.ok(showtimeService.getShowtimeById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShowtime(@PathVariable Long id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatResponse>> getSeats(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.getSeatsByShowtime(id));
    }

    @GetMapping("/{id}/seats/available")
    public ResponseEntity<List<SeatResponse>> getAvailableSeats(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.getAvailableSeatsByShowtime(id));
    }
}