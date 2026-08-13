package com.bandits.bhumisaara.dto.response;

import com.bandits.bhumisaara.enums.TokenType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** One farmer's subsidy credit issuance for one season. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditIssuanceResponseDTO {

    private Long issuanceId;
    private Long farmerId;
    private String farmerName;
    private String farmerWallet;
    private String season;
    private Integer creditsKg;
    private String tokenId;
    /** Always SUBSIDY_CREDIT — a claim on the treasury, not on a warehouse. */
    private TokenType tokenType;
    private String transactionHash;
    private Long issuedByUserId;
    private String issuedByName;
    private LocalDateTime createdAt;
}
