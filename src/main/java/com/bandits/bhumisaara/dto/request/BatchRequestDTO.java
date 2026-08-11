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
public class BatchRequestDTO {

    @NotBlank(message = "token_id is required")
    private String tokenId;

    @NotBlank(message = "transaction_hash is required")
    private String transactionHash;

    @NotBlank(message = "importer_name is required")
    private String importerName;

    @NotBlank(message = "fertilizer_type is required")
    private String fertilizerType;

    @NotNull(message = "volume_kg is required")
    @Positive(message = "volume_kg must be greater than zero")
    private Integer volumeKg;

    @NotNull(message = "minted_by_user_id is required")
    private Long mintedByUserId;
}
