package io.agx.bookmyseat.dto.response;

import io.agx.bookmyseat.entity.Screen;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScreenResponse {

    private Long id;
    private String name;
    private Long theaterId;

    public static ScreenResponse from(Screen screen) {
        return ScreenResponse.builder()
                .id(screen.getId())
                .name(screen.getName())
                .theaterId(screen.getTheater().getId())
                .build();
    }
}