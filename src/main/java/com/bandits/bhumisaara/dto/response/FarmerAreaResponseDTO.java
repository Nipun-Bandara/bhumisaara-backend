package com.bandits.bhumisaara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerAreaResponseDTO {

    private Long userId;

    private String username;

    // All three are null until the farmer picks an area on their profile.
    private Long areaId;

    private String areaName;

    private String district;
}
