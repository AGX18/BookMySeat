package io.agx.bookmyseat.dto.response;

import io.agx.bookmyseat.entity.Theater;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheaterResponse {

    private Long id;
    private String name;
    private String branch;
    private String city;
    private String address;

    public static TheaterResponse from(Theater theater) {
        return TheaterResponse.builder()
                .id(theater.getId())
                .name(theater.getName())
                .branch(theater.getBranch())
                .city(theater.getCity())
                .address(theater.getAddress())
                .build();
    }
}