package com.bandits.bhumisaara.dto.response;

import com.bandits.bhumisaara.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * The operator's one-screen summary of what is wrong with the platform.
 * <p>
 * <strong>This is operational warnings, not analytics.</strong> Every figure
 * below is something a human may have to act on — a queue nobody is working, a
 * wallet that silently breaks the token chain, an area with no officer. Volume
 * and throughput metrics belong on the government's screens, which already
 * have them; adding them here would bury the warnings among numbers nobody
 * needs to do anything about.
 * <p>
 * A zero is the healthy answer for every count in this DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthResponseDTO {

    /** Every role, including the ones with no users, so the shape is stable. */
    private Map<Role, Long> userCountsByRole;

    private long totalUsers;

    // ─── Wallets ─────────────────────────────────────────────────────────────

    private long unlinkedWallets;

    /** No wallet means no area this officer can receive stock for. */
    private long unlinkedOfficerWallets;

    /** No wallet means the ministry cannot mint or issue at all. */
    private long unlinkedGovernmentAdminWallets;

    // ─── Coverage ────────────────────────────────────────────────────────────

    private long vacantAreaCount;

    /** The vacant areas themselves, so the warning is actionable in place. */
    private List<AreaCoverageResponseDTO> vacantAreas;

    // ─── Stale queues ────────────────────────────────────────────────────────

    /**
     * Farmer subsidy requests nobody has reviewed in over a week.
     * <p>
     * The brief called this "SUBMITTED"; this codebase's
     * {@code RequestStatus} calls the unreviewed state {@code PENDING}, and
     * that is what is counted.
     */
    private long staleFertilizerRequests;

    /** Orders a seller has not marked ready in over a week. */
    private long staleMarketOrders;

    /** How old a record has to be to count as stale, in days. */
    private int staleAfterDays;

    // ─── Flagged records ─────────────────────────────────────────────────────

    /** Handovers a farmer says never reached them. Each needs a human. */
    private long disputedDistributions;

    private long bannedUsers;
}
