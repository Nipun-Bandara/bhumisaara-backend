package com.bandits.bhumisaara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One area and the officer serving it, or the absence of one.
 * <p>
 * A vacant area is not merely an empty cell — nobody reviews its farmers'
 * requests and no stock can be transferred there, because
 * {@code BatchTransferService} resolves the recipient from the area. So the
 * farmer and pending-request counts travel with the row: they turn "vacant"
 * into "vacant, and 43 farmers are waiting on 18 unreviewed requests", which
 * is what tells an operator which vacancy to fill first.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaCoverageResponseDTO {

    private Long areaId;

    private String areaName;

    private String district;

    private Boolean isActive;

    // ─── The serving officer, all null when the area is vacant ───────────────

    private Long officerId;

    private String officerUsername;

    private String officerEmail;

    /** Officers with no wallet cannot receive a stock transfer at all. */
    private Boolean officerWalletLinked;

    private Boolean isVacant;

    // ─── What the vacancy costs ──────────────────────────────────────────────

    private long farmerCount;

    /** Requests from those farmers still sitting in PENDING. */
    private long pendingRequestCount;
}
