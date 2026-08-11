package com.bandits.bhumisaara.dto.request;

import com.bandits.bhumisaara.enums.RequestStatus;
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
public class FertilizerRequestReviewDTO {

    /** Must be APPROVED or REJECTED; the service rejects any other value. */
    @NotNull(message = "status is required")
    private RequestStatus status;

    /**
     * Required when status is APPROVED, must be omitted when REJECTED.
     * Allows a partial approval below the requested amount.
     */
    @Positive(message = "approved_kg must be greater than zero")
    private Integer approvedKg;
}
