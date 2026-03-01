package io.agx.bookmyseat.controller;

import io.agx.bookmyseat.dto.request.CreateBookingRequest;
import io.agx.bookmyseat.dto.response.BookingResponse;
import io.agx.bookmyseat.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;


    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }


    @GetMapping("/{confirmationId}")
    public ResponseEntity<BookingResponse> getBookingByConfirmationId(@PathVariable UUID confirmationId) {
        return ResponseEntity.ok(bookingService.getBookingByConfirmationId(confirmationId));
    }

    @PatchMapping("/{confirmationId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable UUID confirmationId) {
        return ResponseEntity.ok(bookingService.cancelBooking(confirmationId));
    }

    // TODO: Replace with GET /api/bookings/me once JWT authentication is implemented
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }


}