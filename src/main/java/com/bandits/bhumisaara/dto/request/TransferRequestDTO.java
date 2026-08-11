package com.bandits.bhumisaara.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Records a completed on-chain transfer of sacks to an agrarian service officer.
 * The sending admin is taken from the JWT, never from this payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDTO {

    @NotNull(message = "batch_id is required")
    private Long batchId;

    @NotBlank(message = "token_id is required")
    private String tokenId;

    @NotNull(message = "to_officer_id is required")
    private Long toOfficerId;

    @NotNull(message = "amount_kg is required")
    @Positive(message = "amount_kg must be greater than zero")
    private Integer amountKg;

    @NotBlank(message = "transaction_hash is required")
    private String transactionHash;

    @NotEmpty(message = "at least one sack serial is required")
    private List<@NotBlank(message = "sack serials cannot be blank") String> sackSerials;
}
