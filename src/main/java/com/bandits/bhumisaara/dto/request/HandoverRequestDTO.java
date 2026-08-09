package com.bandits.bhumisaara.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandoverRequestDTO {

    @NotBlank(message = "token_id is required")
    private String tokenId;

    @NotNull(message = "batch_id is required")
    private Long batchId;

    @NotNull(message = "farmer_id is required")
    private Long farmerId;

    @NotNull(message = "officer_id is required")
    private Long officerId;

    @NotNull(message = "amount_dispensed_kg is required")
    @Positive(message = "amount_dispensed_kg must be greater than zero")
    private Integer amountDispensedKg;

    @NotBlank(message = "burn_transaction_hash is required")
    private String burnTransactionHash;
}
