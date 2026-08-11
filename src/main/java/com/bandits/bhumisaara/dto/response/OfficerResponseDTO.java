package com.bandits.bhumisaara.dto.response;

import com.bandits.bhumisaara.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficerResponseDTO {

    private Long userId;

    private String username;

    private String email;

    private Role role;

    private Boolean isAssigned;

    // Null until the officer has been assigned to an area.
    private Long areaId;

    private String areaName;

    private String district;
}
