package com.bandits.bhumisaara.service;

import com.bandits.bhumisaara.dto.request.AreaRequestDTO;
import com.bandits.bhumisaara.dto.response.AdminAreaResponseDTO;
import com.bandits.bhumisaara.dto.response.AreaCoverageResponseDTO;
import com.bandits.bhumisaara.entity.AreaEntity;
import com.bandits.bhumisaara.entity.UserEntity;
import com.bandits.bhumisaara.enums.RequestStatus;
import com.bandits.bhumisaara.enums.Role;
import com.bandits.bhumisaara.repository.AreaRepository;
import com.bandits.bhumisaara.repository.FertilizerRequestRepository;
import com.bandits.bhumisaara.repository.UserRepository;
import com.bandits.bhumisaara.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Area administration and officer coverage.
 * <p>
 * Areas are the spine of the distribution flow — a farmer's area decides which
 * officer reviews their request, and an officer's area decides where stock is
 * sent — so this class only ever shapes that map. It never moves anything
 * across it.
 */
@Service
@RequiredArgsConstructor
public class AdminAreaService {

    private final AreaRepository areaRepository;
    private final UserRepository userRepository;
    private final FertilizerRequestRepository fertilizerRequestRepository;
    private final CurrentUserProvider currentUserProvider;
    private final AuditService auditService;

    // ─── Coverage ────────────────────────────────────────────────────────────

    /**
     * Every area with its serving officer, vacancies included. Deactivated
     * areas are listed too: an operator managing coverage needs to see what
     * was retired, not just what is live.
     */
    @Transactional(readOnly = true)
    public List<AreaCoverageResponseDTO> getCoverage() {
        return areaRepository.findAllByOrderByDistrictAscAreaNameAsc().stream()
                .map(this::mapToCoverage)
                .toList();
    }

    private AreaCoverageResponseDTO mapToCoverage(AreaEntity area) {
        // Lowest user id wins if historic data left two officers on one area,
        // matching how BatchTransferService picks the delivery recipient — the
        // coverage screen must name the officer who would actually receive.
        UserEntity officer = userRepository
                .findByRole_RoleNameAndArea_AreaIdOrderByUserIdAsc(
                        Role.AGRARIAN_SERVICE_OFFICER, area.getAreaId())
                .stream()
                .findFirst()
                .orElse(null);

        return AreaCoverageResponseDTO.builder()
                .areaId(area.getAreaId())
                .areaName(area.getAreaName())
                .district(area.getDistrict())
                .isActive(area.getIsActive())
                .officerId(officer != null ? officer.getUserId() : null)
                .officerUsername(officer != null ? officer.getUsername() : null)
                .officerEmail(officer != null ? officer.getEmail() : null)
                .officerWalletLinked(officer != null ? officer.getWalletAddress() != null : null)
                .isVacant(officer == null)
                .farmerCount(userRepository.countByRole_RoleNameAndArea_AreaId(Role.FARMER, area.getAreaId()))
                .pendingRequestCount(fertilizerRequestRepository.countByStatusAndFarmer_Area_AreaId(
                        RequestStatus.PENDING, area.getAreaId()))
                .build();
    }

    // ─── CRUD ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AdminAreaResponseDTO> getAreas() {
        return areaRepository.findAllByOrderByDistrictAscAreaNameAsc().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public AdminAreaResponseDTO createArea(AreaRequestDTO request) {
        UserEntity actor = currentUserProvider.require();

        String areaName = request.getAreaName().trim();
        String district = request.getDistrict().trim();

        // Pre-checked as well as constrained, so the admin gets a message that
        // names the duplicate instead of the generic conflict the database
        // violation handler produces.
        if (areaRepository.existsByAreaNameIgnoreCaseAndDistrictIgnoreCase(areaName, district)) {
            throw new IllegalStateException(areaName + ", " + district + " already exists");
        }

        AreaEntity saved = areaRepository.save(AreaEntity.builder()
                .areaName(areaName)
                .district(district)
                .isActive(true)
                .build());

        auditService.record(actor, AuditService.Action.AREA_CREATED, null,
                "area:" + saved.getAreaId(),
                "Created the area " + describe(saved));

        return mapToDTO(saved);
    }

    @Transactional
    public AdminAreaResponseDTO updateArea(Long areaId, AreaRequestDTO request) {
        UserEntity actor = currentUserProvider.require();
        AreaEntity area = requireArea(areaId);

        String areaName = request.getAreaName().trim();
        String district = request.getDistrict().trim();
        String previous = describe(area);

        boolean unchanged = areaName.equalsIgnoreCase(area.getAreaName())
                && district.equalsIgnoreCase(area.getDistrict());

        if (!unchanged
                && areaRepository.existsByAreaNameIgnoreCaseAndDistrictIgnoreCase(areaName, district)) {
            throw new IllegalStateException(areaName + ", " + district + " already exists");
        }

        area.setAreaName(areaName);
        area.setDistrict(district);
        AreaEntity saved = areaRepository.save(area);

        auditService.record(actor, AuditService.Action.AREA_UPDATED, null,
                "area:" + saved.getAreaId(),
                "Renamed the area " + previous + " to " + describe(saved));

        return mapToDTO(saved);
    }

    /**
     * Retires an area without deleting it.
     * <p>
     * A hard delete is not on offer: farmers, requests, transfers and handovers
     * all reference an area, and removing the row would either fail on a
     * foreign key or orphan history that has to stay auditable. Deactivation
     * takes the area out of every picker and leaves the record intact.
     * <p>
     * Refused while anyone still depends on it — retiring an area out from
     * under its officer or its farmers would strand them with no queue and no
     * way to be served.
     */
    @Transactional
    public AdminAreaResponseDTO deactivateArea(Long areaId) {
        UserEntity actor = currentUserProvider.require();
        AreaEntity area = requireArea(areaId);

        if (!Boolean.TRUE.equals(area.getIsActive())) {
            throw new IllegalStateException(describe(area) + " is already deactivated");
        }

        userRepository.findByRole_RoleNameAndArea_AreaIdOrderByUserIdAsc(
                        Role.AGRARIAN_SERVICE_OFFICER, areaId).stream()
                .findFirst()
                .ifPresent(officer -> {
                    throw new IllegalStateException(
                            describe(area) + " is served by " + officer.getUsername()
                                    + ". Unassign them before deactivating the area.");
                });

        long farmers = userRepository.countByRole_RoleNameAndArea_AreaId(Role.FARMER, areaId);
        if (farmers > 0) {
            throw new IllegalStateException(
                    describe(area) + " still has " + farmers + " registered farmer(s). "
                            + "Move them to another area before deactivating this one — otherwise "
                            + "they have no officer and cannot file a request.");
        }

        area.setIsActive(false);
        AreaEntity saved = areaRepository.save(area);

        auditService.record(actor, AuditService.Action.AREA_DEACTIVATED, null,
                "area:" + saved.getAreaId(),
                "Deactivated the area " + describe(saved));

        return mapToDTO(saved);
    }

    /** The inverse of {@link #deactivateArea}, so retirement isn't a one-way door. */
    @Transactional
    public AdminAreaResponseDTO activateArea(Long areaId) {
        UserEntity actor = currentUserProvider.require();
        AreaEntity area = requireArea(areaId);

        if (Boolean.TRUE.equals(area.getIsActive())) {
            throw new IllegalStateException(describe(area) + " is already active");
        }

        area.setIsActive(true);
        AreaEntity saved = areaRepository.save(area);

        auditService.record(actor, AuditService.Action.AREA_REACTIVATED, null,
                "area:" + saved.getAreaId(),
                "Reactivated the area " + describe(saved));

        return mapToDTO(saved);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private AreaEntity requireArea(Long areaId) {
        return areaRepository.findById(areaId)
                .orElseThrow(() -> new IllegalArgumentException("Area not found with id: " + areaId));
    }

    private String describe(AreaEntity area) {
        return area.getAreaName() + ", " + area.getDistrict();
    }

    private AdminAreaResponseDTO mapToDTO(AreaEntity area) {
        return AdminAreaResponseDTO.builder()
                .areaId(area.getAreaId())
                .areaName(area.getAreaName())
                .district(area.getDistrict())
                .isActive(area.getIsActive())
                .build();
    }
}
