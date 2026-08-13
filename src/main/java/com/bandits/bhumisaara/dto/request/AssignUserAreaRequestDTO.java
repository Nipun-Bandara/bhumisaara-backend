package com.bandits.bhumisaara.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Which area an officer serves.
 * <p>
 * {@code areaId} is deliberately nullable and deliberately not
 * {@code @NotNull}: sending null unassigns the officer, which is the only way
 * to clear the area an officer holds — and a prerequisite for changing their
 * role away from {@code AGRARIAN_SERVICE_OFFICER}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignUserAreaRequestDTO {

    private Long areaId;
}
