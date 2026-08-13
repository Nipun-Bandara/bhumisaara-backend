package com.bandits.bhumisaara.service;

/**
 * The one place the subsidy conversion rate is defined.
 * <p>
 * A credit is an entitlement to <strong>1 kg of chemical</strong> fertilizer or
 * <strong>1.5 kg of organic</strong> — the organic advantage is the policy
 * lever that makes organic cheaper to buy with the same budget. The marketplace
 * shows this arithmetic live, but the client's total is never trusted: every
 * order re-derives it here inside the transaction that reserves the stock.
 * <p>
 * All arithmetic is integer. The 1.5 is expressed as {@code × 3 / 2} so a
 * rounding difference between the browser's floating point and the server's can
 * never let a farmer under-pay.
 */
public final class CreditMath {

    private CreditMath() {
    }

    /**
     * Credits needed to cover {@code quantityKg} of this product.
     * <p>
     * Rounded <em>up</em>: a farmer buying 10kg of organic owes 7 credits
     * (6.67 rounded up), because a partial credit cannot be transferred.
     */
    public static int creditsRequired(int quantityKg, boolean isOrganic) {
        if (quantityKg <= 0) {
            return 0;
        }
        // ceil(kg / 1.5) == ceil(2kg / 3). Written out rather than via
        // Math.ceilDiv, which is Java 18+ and this module targets 17.
        return isOrganic ? (quantityKg * 2 + 2) / 3 : quantityKg;
    }

    /**
     * Kilograms {@code credits} will actually pay for.
     * <p>
     * Rounded <em>down</em>, the mirror of {@link #creditsRequired}: credits
     * never buy more than they are worth.
     */
    public static int kgCoveredByCredits(int credits, boolean isOrganic) {
        if (credits <= 0) {
            return 0;
        }
        // floor(credits × 1.5) == credits × 3 / 2
        return isOrganic ? credits * 3 / 2 : credits;
    }
}
