package com.bandits.bhumisaara.dto.request;

import com.bandits.bhumisaara.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The new role for a user. The target user comes from the path and the acting
 * admin from the JWT, so this payload carries nothing but the change itself.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleRequestDTO {

    @NotNull(message = "Role is required")
    private Role role;
}
