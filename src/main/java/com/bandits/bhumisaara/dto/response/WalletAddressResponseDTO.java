package com.bandits.bhumisaara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletAddressResponseDTO {

    private Long userId;

    private String username;

    /** Null until the user connects a wallet for the first time. */
    private String walletAddress;
}
