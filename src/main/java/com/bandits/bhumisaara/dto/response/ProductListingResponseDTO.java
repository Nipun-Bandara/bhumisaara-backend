package com.bandits.bhumisaara.dto.response;

import com.bandits.bhumisaara.enums.ListingStatus;
import com.bandits.bhumisaara.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** One marketplace listing, as both its seller and a browsing farmer see it. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListingResponseDTO {

    private Long listingId;
    private Long sellerId;
    private String sellerName;
    private Role sellerRole;
    /** Null until the seller connects a wallet — they cannot receive credits yet. */
    private String sellerWallet;

    private String productName;
    private String fertilizerType;
    private Boolean isOrganic;
    private String description;
    private Integer priceLkrPerKg;
    private Integer availableKg;
    private Boolean isSubsidyEligible;
    private ListingStatus status;

    /**
     * How many kilograms one credit buys here — 1 for chemical, 1.5 for organic.
     * Sent so the marketplace never hardcodes the policy rate client-side.
     */
    private Double kgPerCredit;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
