package com.bandits.bhumisaara.dto.response;

import com.bandits.bhumisaara.enums.SackStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SackResponseDTO {

    private Long sackId;
    private Long batchId;

    /** Resolved from the batch, so a sack list can be grouped by type on its own. */
    private String fertilizerType;
    private String tokenId;

    private String serial;
    private Integer weightKg;
    private SackStatus status;
    private Long heldByUserId;
    private LocalDateTime createdAt;
}
