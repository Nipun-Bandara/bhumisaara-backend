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
 * Records a handover whose ERC-1155 burn already confirmed on-chain.
 * <p>
 * The officer is taken from the JWT and the farmer from the request being
 * fulfilled — neither is client-supplied, so a caller cannot dispense against
 * someone else's approval or credit the handover to another officer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandoverRequestDTO {

    @NotNull(message = "request_id is required")
    private Long requestId;

    @NotNull(message = "batch_id is required")
    private Long batchId;

    @NotBlank(message = "token_id is required")
    private String tokenId;

    @NotNull(message = "amount_dispensed_kg is required")
    @Positive(message = "amount_dispensed_kg must be greater than zero")
    private Integer amountDispensedKg;

    @NotBlank(message = "burn_transaction_hash is required")
    private String burnTransactionHash;

    @NotEmpty(message = "at least one sack serial is required")
    private List<@NotBlank(message = "sack serials cannot be blank") String> sackSerials;

    /** Checked against the farmer's stored address so stock can't go to the wrong person. */
    @NotBlank(message = "farmer_wallet is required")
    private String farmerWallet;
}
