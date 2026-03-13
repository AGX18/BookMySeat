package io.agx.bookmyseat.controller;

import io.agx.bookmyseat.dto.request.CreateBookingRequest;
import io.agx.bookmyseat.dto.response.BookingResponse;
import io.agx.bookmyseat.service.BookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }


    @GetMapping("/{confirmationId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<BookingResponse> getBookingByConfirmationId(@PathVariable UUID confirmationId) {
        return ResponseEntity.ok(bookingService.getBookingByConfirmationId(confirmationId));
    }

    @PatchMapping("/{confirmationId}/cancel")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable UUID confirmationId) {
        return ResponseEntity.ok(bookingService.cancelBooking(confirmationId));
    }

    // TODO: Replace with GET /api/bookings/me once JWT authentication is implemented
    @GetMapping("/user/{userId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }


}