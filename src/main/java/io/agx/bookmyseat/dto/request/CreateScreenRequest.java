package io.agx.bookmyseat.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateScreenRequest {

    @NotBlank(message = "Screen name is required")
    private String name;
}