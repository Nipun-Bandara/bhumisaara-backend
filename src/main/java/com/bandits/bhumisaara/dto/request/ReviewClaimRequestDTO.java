package com.bandits.bhumisaara.dto.request;

import com.bandits.bhumisaara.enums.ClaimStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The government's decision on a redemption claim. The reviewing admin comes
 * from the JWT, never from this payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewClaimRequestDTO {

    /** Must be APPROVED or REJECTED; PAID is reached by the seller's burn. */
    @NotNull(message = "status is required")
    private ClaimStatus status;
}
