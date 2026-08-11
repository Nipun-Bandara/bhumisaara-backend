package com.bandits.bhumisaara.dto.response;

import com.bandits.bhumisaara.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FertilizerRequestResponseDTO {

    private Long requestId;

    private Long farmerId;

    private String farmerUsername;

    private String areaName;

    private String district;

    private String season;

    private String fertilizerType;

    private Integer requestedKg;

    private Integer approvedKg;

    private RequestStatus status;

    private Long reviewedByOfficerId;

    private String reviewedByOfficerUsername;

    private LocalDateTime reviewedAt;

    // Collection fields — null until the request is collected (not yet wired).
    private Long batchId;

    private String sackSerial;

    private String burnTxHash;

    private LocalDateTime collectedAt;

    private LocalDateTime createdAt;
}
