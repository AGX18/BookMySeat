package io.agx.bookmyseat.dto.response;

import io.agx.bookmyseat.entity.Showtime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeResponse {

    private Long id;

    // Flat mapping the related entities for frontend convenience
    private Long movieId;
    private String movieTitle;

    private Integer movieDurationMins;


    private Long screenId;
    private String screenName;

    private Long theaterId;
    private String theaterName;
    private String theaterBranch;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public static ShowtimeResponse from(Showtime showtime) {
        return ShowtimeResponse.builder()
                .id(showtime.getId())
                .movieId(showtime.getMovie().getId())
                .movieTitle(showtime.getMovie().getTitle())
                .movieDurationMins(showtime.getMovie().getDurationMins())
                .screenId(showtime.getScreen().getId())
                .screenName(showtime.getScreen().getName())
                .theaterId(showtime.getScreen().getTheater().getId())
                .theaterName(showtime.getScreen().getTheater().getName())
                .theaterBranch(showtime.getScreen().getTheater().getBranch())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .build();
    }
}