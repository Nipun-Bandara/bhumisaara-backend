package com.bandits.bhumisaara.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Records a subsidy credit mint that has already confirmed on-chain.
 * The issuing admin comes from the JWT, never from this payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueCreditsRequestDTO {

    @NotNull(message = "farmer_id is required")
    private Long farmerId;

    @NotBlank(message = "season is required")
    @Size(max = 50, message = "season must be 50 characters or fewer")
    private String season;

    @NotNull(message = "credits_kg is required")
    @Positive(message = "credits_kg must be greater than zero")
    private Integer creditsKg;

    /**
     * The ERC-1155 id the credits were minted under. Must match the id the
     * season already uses, if any — the server rejects a mismatch rather than
     * silently splitting a season's credits across two tokens.
     */
    @NotBlank(message = "token_id is required")
    private String tokenId;

    @NotBlank(message = "transaction_hash is required")
    private String transactionHash;
}
