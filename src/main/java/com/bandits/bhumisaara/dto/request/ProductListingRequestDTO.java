package com.bandits.bhumisaara.dto.request;

import com.bandits.bhumisaara.enums.ListingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create or replace one of the calling seller's own listings.
 * <p>
 * There is deliberately no {@code sellerId} and no {@code isOrganic}: ownership
 * comes from the JWT and the organic flag from the owner's role, so neither can
 * be forged by editing a request body.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListingRequestDTO {

    @NotBlank(message = "product_name is required")
    @Size(max = 120, message = "product_name must be 120 characters or fewer")
    private String productName;

    @NotBlank(message = "fertilizer_type is required")
    @Size(max = 60, message = "fertilizer_type must be 60 characters or fewer")
    private String fertilizerType;

    @Size(max = 1000, message = "description must be 1000 characters or fewer")
    private String description;

    @NotNull(message = "price_lkr_per_kg is required")
    @Positive(message = "price_lkr_per_kg must be greater than zero")
    private Integer priceLkrPerKg;

    /** Zero is allowed — a seller may list a product that is temporarily out of stock. */
    @NotNull(message = "available_kg is required")
    @PositiveOrZero(message = "available_kg cannot be negative")
    private Integer availableKg;

    @NotNull(message = "is_subsidy_eligible is required")
    private Boolean isSubsidyEligible;

    /**
     * Optional on create (defaults to ACTIVE). SOLD_OUT is derived from
     * {@code availableKg} and is rejected if sent explicitly.
     */
    private ListingStatus status;
}
