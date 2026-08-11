package com.bandits.bhumisaara.enums;

public enum RequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
    /** Some of the approved amount has been handed over; the rest is still owed. */
    PARTIALLY_COLLECTED,
    COLLECTED
}
