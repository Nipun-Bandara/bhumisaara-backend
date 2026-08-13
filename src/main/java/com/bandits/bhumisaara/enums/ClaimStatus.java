package com.bandits.bhumisaara.enums;

/**
 * Lifecycle of a seller's redemption claim against the treasury.
 * <p>
 * The burn happens between {@link #APPROVED} and {@link #PAID}: the credits sit
 * in the seller's own wallet, so only the seller's wallet can destroy them. The
 * admin's approval authorises the payment; the seller's burn settles the claim.
 */
public enum ClaimStatus {

    /** Raised by the seller, awaiting government review. */
    SUBMITTED,

    /** Approved for payment. The seller must now burn the credits to settle. */
    APPROVED,

    /** Credits burned on-chain and the claim settled. */
    PAID,

    /** Refused by the government. Nothing is burned; the credits stay with the seller. */
    REJECTED
}
