package io.agx.bookmyseat.service;

import io.agx.bookmyseat.entity.*;
import io.agx.bookmyseat.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final UserRepository userRepository;

    public Optional<Booking> getBookingByConfirmationId(UUID confirmationId) {
        return bookingRepository.findByConfirmationIdWithDetails(confirmationId);
    }

    public List<Booking> getBookingsByUser(UUID userId) {
        return bookingRepository.findByUserIdWithDetails(userId);
    }

    @Transactional
    public Booking createBooking(UUID userId, Long showtimeId, List<Long> seatIds) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

            Showtime showtime = showtimeRepository.findById(showtimeId)
                    .orElseThrow(() -> new IllegalArgumentException("Showtime not found: " + showtimeId));

            if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("Cannot book a showtime that has already started");
            }

            List<Seat> seats = seatRepository.findAvailableSeatsByIds(showtimeId, seatIds);
            if (seats.size() != seatIds.size()) {
                throw new IllegalStateException("One or more seats are no longer available");
            }

            // update individually so @Version check fires for each seat
            seats.forEach(seat -> seat.setStatus(SeatStatus.BOOKED));
            seatRepository.saveAll(seats);


            Booking booking = Booking.builder()
                    .user(user)
                    .showtime(showtime)
                    .seats(new HashSet<>(seats))
                    .status(BookingStatus.CONFIRMED)
                    .build();

            return bookingRepository.save(booking);
    } catch (ObjectOptimisticLockingFailureException e) {
        throw new IllegalStateException("One or more seats were just booked by someone else, please try again");
    }

    }

    @Transactional
    public Booking cancelBooking(UUID confirmationId) {
        Booking booking = bookingRepository.findByConfirmationIdWithDetails(confirmationId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + confirmationId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        if (booking.getShowtime().getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot cancel a booking after the showtime has started");
        }

        List<Long> seatIds = booking.getSeats()
                .stream()
                .map(Seat::getId)
                .toList();

        seatRepository.updateStatusForSeats(seatIds, SeatStatus.AVAILABLE);

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }
}