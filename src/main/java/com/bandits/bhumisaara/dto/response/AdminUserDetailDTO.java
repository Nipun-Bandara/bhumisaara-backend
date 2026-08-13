package com.bandits.bhumisaara.dto.response;

import com.bandits.bhumisaara.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * The full picture of one account, for the screen an admin lands on before
 * banning, demoting or reassigning someone.
 * <p>
 * The activity counts are the point of this DTO: "ban this user" reads very
 * differently next to <em>0 records</em> than next to <em>40 handovers and 12
 * open orders</em>. They are what the guard rails in
 * {@code AdminUserService.changeRole} check, surfaced so the admin can see a
 * blocker before hitting it.
 * <p>
 * Still no password field, and the wallet address stays truncated — an admin
 * has no legitimate use for the full string, since they cannot set one.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailDTO {

    private Long userId;

    private String username;

    private String email;

    private Role role;

    private String fullName;

    private String address;

    private String contactNumber;

    private Long areaId;

    private String areaName;

    private String district;

    private Boolean isAssigned;

    private String walletAddressTruncated;

    private Boolean walletLinked;

    private Boolean isBanned;

    private LocalDateTime createdAt;

    // ─── Activity: what this account owns in the domain ──────────────────────

    /** Subsidy requests filed as a farmer. */
    private long fertilizerRequestCount;

    /** Requests reviewed as an agrarian service officer. */
    private long requestsReviewedCount;

    /** Handovers received as a farmer plus handovers performed as an officer. */
    private long distributionCount;

    /** Marketplace orders placed as a farmer plus orders received as a seller. */
    private long orderCount;

    /** Marketplace listings owned as a seller. */
    private long listingCount;
}
