package io.agx.bookmyseat.dto.response;

import io.agx.bookmyseat.entity.Seat;
import io.agx.bookmyseat.entity.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {
    private Long id;
    private Character row;
    private Integer number;
    private SeatStatus status;

    public static SeatResponse from(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .row(seat.getRow())
                .number(seat.getNumber())
                .status(seat.getStatus())
                .build();
    }
}