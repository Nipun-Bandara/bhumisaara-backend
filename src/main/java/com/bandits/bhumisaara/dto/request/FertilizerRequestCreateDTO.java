package com.bandits.bhumisaara.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FertilizerRequestCreateDTO {

    @NotBlank(message = "season is required")
    private String season;

    @NotBlank(message = "fertilizer_type is required")
    private String fertilizerType;

    @NotNull(message = "requested_kg is required")
    @Positive(message = "requested_kg must be greater than zero")
    private Integer requestedKg;
}
