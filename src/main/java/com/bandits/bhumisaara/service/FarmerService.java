package com.bandits.bhumisaara.service;

import com.bandits.bhumisaara.dto.request.AssignFarmerAreaRequestDTO;
import com.bandits.bhumisaara.dto.response.FarmerAreaResponseDTO;
import com.bandits.bhumisaara.entity.AreaEntity;
import com.bandits.bhumisaara.entity.UserEntity;
import com.bandits.bhumisaara.repository.AreaRepository;
import com.bandits.bhumisaara.repository.UserRepository;
import com.bandits.bhumisaara.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FarmerService {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final AreaRepository areaRepository;

    @Transactional(readOnly = true)
    public FarmerAreaResponseDTO getMyArea() {
        return mapToDTO(currentUserProvider.require());
    }

    /**
     * Sets the authenticated farmer's own area. This is what routes their
     * fertilizer requests to a review queue — an officer only sees requests from
     * farmers in the officer's area.
     * <p>
     * Changing area with requests already pending moves those requests to the
     * new area's queue, since the review check reads the farmer's current area.
     */
    @Transactional
    public FarmerAreaResponseDTO updateMyArea(AssignFarmerAreaRequestDTO request) {
        UserEntity farmer = currentUserProvider.require();

        AreaEntity area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Area not found with id: " + request.getAreaId()));

        farmer.setArea(area);
        farmer.setIsAssigned(true);

        return mapToDTO(userRepository.save(farmer));
    }

    private FarmerAreaResponseDTO mapToDTO(UserEntity farmer) {
        AreaEntity area = farmer.getArea();

        return FarmerAreaResponseDTO.builder()
                .userId(farmer.getUserId())
                .username(farmer.getUsername())
                .areaId(area != null ? area.getAreaId() : null)
                .areaName(area != null ? area.getAreaName() : null)
                .district(area != null ? area.getDistrict() : null)
                .build();
    }
}
