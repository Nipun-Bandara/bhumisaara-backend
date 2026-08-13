package com.bandits.bhumisaara.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A farmer placing an order. No tokens move — this only reserves stock.
 * <p>
 * Note what is <em>not</em> here: no cash total and no credit requirement. The
 * client shows a live breakdown, but the server re-derives every figure from
 * the listing's price and organic flag. Only {@code creditsUsed} is the
 * farmer's choice, and it is bounded server-side by what the quantity is worth.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequestDTO {

    @NotNull(message = "listing_id is required")
    private Long listingId;

    @NotNull(message = "quantity_kg is required")
    @Positive(message = "quantity_kg must be greater than zero")
    private Integer quantityKg;

    /** Zero for a cash-only purchase; the balance of the quantity is paid in cash. */
    @NotNull(message = "credits_used is required")
    @PositiveOrZero(message = "credits_used cannot be negative")
    private Integer creditsUsed;
}
