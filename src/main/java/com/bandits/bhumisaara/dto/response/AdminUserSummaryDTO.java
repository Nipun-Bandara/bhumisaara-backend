package com.bandits.bhumisaara.dto.response;

import com.bandits.bhumisaara.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * One row of the admin user list.
 * <p>
 * Carries no password field of any kind, and only a <em>truncated</em> wallet
 * address: a list view is the easiest thing in the app to scrape, and a full
 * address here would hand an attacker every wallet on the platform in one
 * request. The truncation is done server-side so it cannot be undone by
 * reading the raw response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserSummaryDTO {

    private Long userId;

    private String username;

    private String email;

    private Role role;

    private String areaName;

    private String district;

    /** {@code 0x1234…abcd}, or null when no wallet is linked. */
    private String walletAddressTruncated;

    private Boolean walletLinked;

    private Boolean isBanned;

    private LocalDateTime createdAt;
}
