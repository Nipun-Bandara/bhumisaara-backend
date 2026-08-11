package com.bandits.bhumisaara.enums;

/**
 * Where a physical sack currently sits in the chain of custody.
 *
 * <ul>
 *   <li>{@code AT_CENTRAL} — held at the central store by the minting government admin.</li>
 *   <li>{@code WITH_OFFICER} — transferred to an agrarian service officer.</li>
 *   <li>{@code DELIVERED} — handed over to a farmer (written by the officer → farmer flow).</li>
 * </ul>
 */
public enum SackStatus {
    AT_CENTRAL,
    WITH_OFFICER,
    DELIVERED
}
