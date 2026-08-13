package com.bandits.bhumisaara.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An administrative password reset, for a user who has lost access.
 * <p>
 * The value is BCrypt-hashed before it touches the database and is never
 * logged, echoed in the response, or written into the audit trail — the audit
 * row records only that a reset happened, and by whom.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequestDTO {

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    private String newPassword;
}
