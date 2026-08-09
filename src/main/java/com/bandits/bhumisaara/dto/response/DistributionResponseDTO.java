package com.bandits.bhumisaara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributionResponseDTO {

    private Long distributionId;
    private String tokenId;
    private Long batchId;
    private Long farmerId;
    private Long officerId;
    private Integer amountDispensedKg;
    private String burnTransactionHash;
    private LocalDateTime createdAt;
}
