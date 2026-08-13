package com.bandits.bhumisaara.enums;

/**
 * Lifecycle of a marketplace order.
 * <p>
 * The ordering matters: a seller can move an order to {@link #CONFIRMED} and no
 * further. Only the farmer can reach {@link #COMPLETED}, and only that step
 * moves credits on-chain. This is the anti-fraud mechanism — a seller can never
 * pull credits out of a farmer's wallet unilaterally.
 */
public enum OrderStatus {

    /** Placed by the farmer. Stock is reserved; no tokens have moved. */
    PENDING_CONFIRMATION,

    /**
     * The <em>seller</em> has confirmed the goods are ready for collection.
     * This is not the farmer's confirmation and moves nothing on-chain.
     */
    CONFIRMED,

    /**
     * The farmer confirmed receipt at physical handover, which is what
     * triggered the credit transfer to the seller.
     */
    COMPLETED,

    /** Cancelled before completion by either party; the reserved stock is restored. */
    CANCELLED,

    /** The farmer's "this was not what I received" flag on a completed order. */
    DISPUTED
}
