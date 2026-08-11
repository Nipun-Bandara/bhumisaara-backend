package com.bandits.bhumisaara.dto.response;

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
public class TransferResponseDTO {

    private Long transferId;
    private Long batchId;
    private String tokenId;
    private String importerName;
    private String fertilizerType;

    private Long fromUserId;
    private String fromUsername;

    private Long toOfficerId;
    private String toOfficerName;
    private String toOfficerWallet;
    private String areaName;
    private String district;

    private Integer amountKg;
    private String transactionHash;

    /** Only populated on the response to a freshly recorded transfer. */
    private List<String> sackSerials;

    private LocalDateTime createdAt;
}
