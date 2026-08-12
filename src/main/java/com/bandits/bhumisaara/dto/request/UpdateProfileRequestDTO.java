package com.bandits.bhumisaara.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The profile details any signed-in user can maintain about themselves.
 * <p>
 * Deliberately the same three fields for every role — the farmer's home address
 * and the officer's agrarian centre are the same column with a different label
 * on screen. Username, email and role are not editable here: they are identity,
 * not profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequestDTO {

    @NotBlank(message = "full_name is required")
    @Size(max = 120, message = "full_name cannot exceed 120 characters")
    private String fullName;

    @NotBlank(message = "address is required")
    @Size(max = 255, message = "address cannot exceed 255 characters")
    private String address;

    /**
     * Kept deliberately permissive — Sri Lankan numbers are written as
     * {@code 0771234567}, {@code +94 77 123 4567} and several ways in between,
     * and rejecting a valid number is worse than storing an odd one.
     */
    @NotBlank(message = "contact_number is required")
    @Pattern(
            regexp = "^\\+?[0-9][0-9 -]{6,19}$",
            message = "contact_number must be a phone number, digits only apart from +, spaces and dashes")
    private String contactNumber;
}
