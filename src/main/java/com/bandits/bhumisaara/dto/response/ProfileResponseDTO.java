package com.bandits.bhumisaara.dto.response;

import com.bandits.bhumisaara.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Everything a user sees about their own account on the profile screen:
 * the identity they signed up with, the details they can edit, and the two
 * things set elsewhere (wallet and area) shown read-only for context.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponseDTO {

    // ─── Identity (not editable here) ────────────────────────────────────────
    private Long userId;
    private String username;
    private String email;
    private Role role;

    // ─── Editable details ────────────────────────────────────────────────────
    /** Null until the user fills the profile in for the first time. */
    private String fullName;
    private String address;
    private String contactNumber;

    // ─── Set by other flows, shown for context ───────────────────────────────
    /** Linked by the wallet sync, not by this screen. */
    private String walletAddress;
    /** Farmers set this on their own profile; officers are assigned by an admin. */
    private Long areaId;
    private String areaName;
    private String district;

    private LocalDateTime createdAt;
}
