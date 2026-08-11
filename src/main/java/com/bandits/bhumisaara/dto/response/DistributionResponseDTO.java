package com.bandits.bhumisaara.dto.response;

import com.bandits.bhumisaara.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributionResponseDTO {

    private Long distributionId;
    private Long requestId;
    private String tokenId;
    private Long batchId;
    private String fertilizerType;

    private Long farmerId;
    private String farmerName;
    private Long officerId;
    private String officerName;

    private Integer amountDispensedKg;
    private String burnTransactionHash;

    /** The physical sacks this handover consumed. */
    private List<String> sackSerials;

    /** State of the request after this handover. */
    private RequestStatus requestStatus;

    private Boolean disputed;
    private LocalDateTime disputedAt;

    private LocalDateTime createdAt;
}
