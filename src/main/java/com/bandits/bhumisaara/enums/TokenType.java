package com.bandits.bhumisaara.enums;

/**
 * The two kinds of token that live on the same ERC-1155 contract.
 * <p>
 * They must never be treated as interchangeable. A {@link #STOCK} token is a
 * claim on a warehouse — it was minted against a real imported batch and is
 * burned when a farmer physically collects the fertilizer. A
 * {@link #SUBSIDY_CREDIT} is a claim on the treasury — it is minted when the
 * government funds a season's subsidy budget, is backed by no goods at all, and
 * is burned only when a seller redeems it for payment.
 * <p>
 * Every table that records a mint carries this column so the two can never be
 * summed together by accident.
 */
public enum TokenType {

    /** Backed by physical fertilizer. 1 token = 1 kg in a warehouse. */
    STOCK,

    /**
     * Backed by the treasury. 1 credit = an entitlement to 1 kg of chemical
     * fertilizer, or 1.5 kg of organic — see {@code CreditMath}.
     */
    SUBSIDY_CREDIT
}
