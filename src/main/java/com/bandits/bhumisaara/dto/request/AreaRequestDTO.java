package com.bandits.bhumisaara.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An agrarian service area, on create and on update alike — both carry the
 * same two fields, so one DTO serves both rather than a pair that would drift.
 * <p>
 * {@code isActive} is deliberately absent: activation is a state change with
 * its own preconditions (an area with an officer or farmers cannot be
 * retired), so it goes through the deactivate/activate endpoints rather than
 * riding along in a general update.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaRequestDTO {

    @NotBlank(message = "Area name is required")
    @Size(max = 100, message = "Area name must be at most 100 characters")
    private String areaName;

    @NotBlank(message = "District is required")
    @Size(max = 100, message = "District must be at most 100 characters")
    private String district;
}
