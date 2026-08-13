package com.bandits.bhumisaara.enums;

/** Lifecycle of a seller's product listing in the marketplace. */
public enum ListingStatus {

    /** Visible to farmers and orderable. */
    ACTIVE,

    /** Hidden from the marketplace by the seller; stock is untouched. */
    PAUSED,

    /**
     * No quantity left. Set automatically when {@code available_kg} reaches
     * zero, and cleared back to ACTIVE when the seller restocks.
     */
    SOLD_OUT
}
