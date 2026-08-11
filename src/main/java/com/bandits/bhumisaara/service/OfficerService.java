package com.bandits.bhumisaara.service;

import com.bandits.bhumisaara.dto.request.AssignOfficerRequestDTO;
import com.bandits.bhumisaara.dto.response.OfficerResponseDTO;
import com.bandits.bhumisaara.entity.AreaEntity;
import com.bandits.bhumisaara.entity.UserEntity;
import com.bandits.bhumisaara.enums.Role;
import com.bandits.bhumisaara.repository.AreaRepository;
import com.bandits.bhumisaara.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfficerService {

    private static final Role OFFICER_ROLE = Role.AGRARIAN_SERVICE_OFFICER;

    private final UserRepository userRepository;
    private final AreaRepository areaRepository;

    /**
     * Lists every user holding the AGRARIAN_SERVICE_OFFICER role.
     *
     * @param assigned when null, all officers are returned; otherwise only those
     *                 whose current assignment state matches.
     */
    @Transactional(readOnly = true)
    public List<OfficerResponseDTO> getOfficers(Boolean assigned) {
        List<UserEntity> officers = assigned == null
                ? userRepository.findByRole_RoleNameOrderByUsernameAsc(OFFICER_ROLE)
                : userRepository.findByRole_RoleNameAndIsAssignedOrderByUsernameAsc(OFFICER_ROLE, assigned);

        return officers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Assigns an agrarian service officer to an area. Re-assigning an already
     * assigned officer moves them to the new area rather than failing, so a
     * mistaken assignment can be corrected without an unassign endpoint.
     */
    @Transactional
    public OfficerResponseDTO assignOfficerToArea(AssignOfficerRequestDTO request) {
        UserEntity officer = userRepository.findById(request.getOfficerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with id: " + request.getOfficerId()));

        if (officer.getRole() == null || officer.getRole().getRoleName() != OFFICER_ROLE) {
            throw new IllegalArgumentException(
                    "User " + request.getOfficerId() + " does not hold the " + OFFICER_ROLE + " role");
        }

        AreaEntity area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Area not found with id: " + request.getAreaId()));

        officer.setArea(area);
        officer.setIsAssigned(true);

        return mapToDTO(userRepository.save(officer));
    }

    private OfficerResponseDTO mapToDTO(UserEntity user) {
        AreaEntity area = user.getArea();

        return OfficerResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getRoleName() : null)
                .isAssigned(user.getIsAssigned())
                .areaId(area != null ? area.getAreaId() : null)
                .areaName(area != null ? area.getAreaName() : null)
                .district(area != null ? area.getDistrict() : null)
                .build();
    }
}
