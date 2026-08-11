package com.bandits.bhumisaara.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignOfficerRequestDTO {

    @NotNull(message = "officer_id is required")
    private Long officerId;

    @NotNull(message = "area_id is required")
    private Long areaId;
}
