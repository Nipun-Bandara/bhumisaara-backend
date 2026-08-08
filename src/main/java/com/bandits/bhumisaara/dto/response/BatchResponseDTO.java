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
public class BatchResponseDTO {

    private Long batchId;
    private String tokenId;
    private String transactionHash;
    private String importerName;
    private String fertilizerType;
    private Integer volumeKg;
    private Long mintedByUserId;
    private LocalDateTime createdAt;
}
