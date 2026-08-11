package com.bandits.bhumisaara.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Checks a scanned sack before the officer commits to burning anything.
 * <p>
 * Purely advisory — every rule here is re-run inside
 * {@code POST /api/v1/distributions}, because a client that skipped this call
 * (or lied about {@code alreadyScannedKg}) must not get further.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SackValidationRequestDTO {

    @NotBlank(message = "sack_serial is required")
    private String sackSerial;

    @NotNull(message = "request_id is required")
    private Long requestId;

    /** Weight of the sacks already scanned in this session, excluding this one. */
    @NotNull(message = "already_scanned_kg is required")
    @PositiveOrZero(message = "already_scanned_kg cannot be negative")
    private Integer alreadyScannedKg;
}
