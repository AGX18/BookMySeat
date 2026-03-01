package io.agx.bookmyseat.dto.response;

import io.agx.bookmyseat.entity.Booking;
import io.agx.bookmyseat.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private UUID confirmationId;
    private UUID userId;
    private String userName;

    private Long showtimeId;
    private String movieTitle;
    private String theaterName;
    private String theaterBranch;
    private String screenName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<SeatResponse> seats;
    private LocalDateTime bookingTime;
    private BookingStatus status;

    public static BookingResponse from(Booking booking) {
        return BookingResponse.builder()
                .confirmationId(booking.getConfirmationId())
                .userId(booking.getUser().getId())
                .userName(booking.getUser().getName())
                .showtimeId(booking.getShowtime().getId())
                .movieTitle(booking.getShowtime().getMovie().getTitle())
                .theaterName(booking.getShowtime().getScreen().getTheater().getName())
                .theaterBranch(booking.getShowtime().getScreen().getTheater().getBranch())
                .screenName(booking.getShowtime().getScreen().getName())
                .startTime(booking.getShowtime().getStartTime())
                .endTime(booking.getShowtime().getEndTime())
                .seats(booking.getSeats().stream().map(SeatResponse::from).toList())
                .status(booking.getStatus())
                .bookingTime(booking.getCreatedAt())
                .build();
    }


}