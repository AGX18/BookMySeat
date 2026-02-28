package io.agx.bookmyseat.dto.response;

import io.agx.bookmyseat.entity.Movie;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class MovieResponse {

    private Long id;
    private String title;
    private String description;
    private Integer durationMins;
    private LocalDate releaseDate;
    private String genre;

    public static MovieResponse from(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .durationMins(movie.getDurationMins())
                .releaseDate(movie.getReleaseDate())
                .genre(movie.getGenre())
                .build();
    }
}