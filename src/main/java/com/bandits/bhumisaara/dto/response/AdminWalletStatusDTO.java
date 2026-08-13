package com.bandits.bhumisaara.dto.response;

import com.bandits.bhumisaara.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One account's wallet link status.
 * <p>
 * The address is truncated, never full: this is a list endpoint, and the whole
 * platform's wallet map should not be one request away. An operator has no
 * need for the full string in any case — they can clear a link but never set
 * one.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminWalletStatusDTO {

    private Long userId;

    private String username;

    private String email;

    private Role role;

    private String areaName;

    private String walletAddressTruncated;

    private Boolean walletLinked;

    /**
     * True when this missing wallet actively breaks a flow rather than merely
     * being incomplete.
     * <p>
     * The backend signs nothing — every mint, transfer and burn is signed in
     * the browser and only <em>recorded</em> here — so a government admin or
     * an officer without a wallet is a dead end in the token chain: the
     * ministry cannot mint, and stock cannot be transferred to that area
     * because the transfer needs an address to send to. A farmer or seller
     * without one simply cannot receive credits yet, which is normal until
     * they first connect.
     */
    private Boolean blocking;
}
