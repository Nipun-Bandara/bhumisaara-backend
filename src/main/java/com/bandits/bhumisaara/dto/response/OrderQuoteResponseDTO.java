package com.bandits.bhumisaara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The server's costing of a proposed order.
 * <p>
 * The marketplace computes the same breakdown live as the farmer types, but
 * this is the figure that actually binds — the browser's arithmetic is a
 * preview, and {@code POST /orders} re-derives all of it again anyway.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderQuoteResponseDTO {

    private Long listingId;
    private Integer quantityKg;
    private Boolean isOrganic;

    /** Credits needed to cover the whole quantity — the ceiling on credits_used. */
    private Integer creditsRequiredForFullQuantity;

    private Integer creditsUsed;
    /** Kilograms those credits actually pay for. */
    private Integer kgCoveredByCredits;
    /** The balance, charged in cash at the listing's price. */
    private Integer kgPaidInCash;
    private Integer cashAmountLkr;

    /** 1.0 for chemical, 1.5 for organic — the organic advantage, in one number. */
    private Double kgPerCredit;
}
