package io.agx.bookmyseat.service;

import io.agx.bookmyseat.dto.request.CreateBookingRequest;
import io.agx.bookmyseat.dto.response.BookingResponse;
import io.agx.bookmyseat.entity.*;
import io.agx.bookmyseat.exception.BookingAlreadyCancelledException;
import io.agx.bookmyseat.exception.ResourceNotFoundException;
import io.agx.bookmyseat.exception.SeatsNotAvailableException;
import io.agx.bookmyseat.exception.ShowtimeAlreadyStartedException;
import io.agx.bookmyseat.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final UserRepository userRepository;

    public BookingResponse getBookingByConfirmationId(UUID confirmationId) {
        Booking booking = bookingRepository.findByConfirmationIdWithDetails(confirmationId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with confirmation ID: " + confirmationId));
        return BookingResponse.from(booking);
    }


    public List<BookingResponse> getBookingsByUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", request.getShowtimeId()));

        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ShowtimeAlreadyStartedException();
        }

        List<Seat> seats = seatRepository.findAvailableSeatsByIds(request.getShowtimeId(), request.getSeatIds());
        Set<Seat> dedupSeats = new HashSet<>(seats);
        if (dedupSeats.size() != request.getSeatIds().size()) {
            throw new SeatsNotAvailableException();
        }

        seats.forEach(seat -> seat.setStatus(SeatStatus.BOOKED));
        seatRepository.saveAll(seats);

        Booking booking = Booking.builder()
                .confirmationId(UUID.randomUUID())
                .user(user)
                .showtime(showtime)
                .seats(dedupSeats)
                .status(BookingStatus.CONFIRMED)
                .build();

        return BookingResponse.from(bookingRepository.save(booking));
    }


    @Transactional
    public BookingResponse cancelBooking(UUID confirmationId) {
        Booking booking = bookingRepository.findByConfirmationIdWithDetails(confirmationId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with confirmation ID: " + confirmationId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingAlreadyCancelledException();
        }

        if (booking.getShowtime().getStartTime().isBefore(LocalDateTime.now())) {
            throw new ShowtimeAlreadyStartedException();
        }

        booking.getSeats().forEach(seat -> seat.setStatus(SeatStatus.AVAILABLE));
        seatRepository.saveAll(booking.getSeats());

        booking.setStatus(BookingStatus.CANCELLED);
        return BookingResponse.from(bookingRepository.save(booking));
    }

}