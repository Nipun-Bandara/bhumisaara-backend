package com.bandits.bhumisaara.service;

import com.bandits.bhumisaara.dto.response.AdminWalletStatusDTO;
import com.bandits.bhumisaara.dto.response.AreaCoverageResponseDTO;
import com.bandits.bhumisaara.dto.response.SystemHealthResponseDTO;
import com.bandits.bhumisaara.entity.AreaEntity;
import com.bandits.bhumisaara.entity.UserEntity;
import com.bandits.bhumisaara.enums.OrderStatus;
import com.bandits.bhumisaara.enums.RequestStatus;
import com.bandits.bhumisaara.enums.Role;
import com.bandits.bhumisaara.repository.DistributionLogRepository;
import com.bandits.bhumisaara.repository.FertilizerRequestRepository;
import com.bandits.bhumisaara.repository.MarketOrderRepository;
import com.bandits.bhumisaara.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Read-only platform oversight: wallet link status and the operational health
 * summary. Nothing here mutates anything, so nothing here is audited.
 */
@Service
@RequiredArgsConstructor
public class AdminPlatformService {

    /**
     * Roles whose missing wallet breaks a flow outright.
     * <p>
     * The backend holds no web3 client — it records confirmed hashes rather
     * than signing anything — so an unlinked wallet is not a cosmetic gap for
     * these two. A government admin cannot mint a batch or issue credits
     * without one, and stock cannot be transferred to an officer who has no
     * address to receive it, which strands their whole area.
     */
    private static final Set<Role> WALLET_CRITICAL_ROLES =
            EnumSet.of(Role.GOVERNMENT_ADMIN, Role.AGRARIAN_SERVICE_OFFICER);

    /** A queue untouched for this long has stopped being a queue. */
    private static final int STALE_AFTER_DAYS = 7;

    private final UserRepository userRepository;
    private final FertilizerRequestRepository fertilizerRequestRepository;
    private final MarketOrderRepository marketOrderRepository;
    private final DistributionLogRepository distributionLogRepository;
    private final AdminAreaService adminAreaService;

    // ─── Wallets ─────────────────────────────────────────────────────────────

    /**
     * Wallet link status for every account.
     *
     * @param unlinkedOnly when true, drops everyone who has already linked one
     */
    @Transactional(readOnly = true)
    public List<AdminWalletStatusDTO> getWalletStatuses(boolean unlinkedOnly) {
        return userRepository.findAllByOrderByUsernameAsc().stream()
                .filter(user -> !unlinkedOnly || user.getWalletAddress() == null)
                .map(this::mapToWalletStatus)
                .toList();
    }

    private AdminWalletStatusDTO mapToWalletStatus(UserEntity user) {
        Role role = user.getRole() != null ? user.getRole().getRoleName() : null;
        AreaEntity area = user.getArea();
        boolean linked = user.getWalletAddress() != null;

        return AdminWalletStatusDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(role)
                .areaName(area != null ? area.getAreaName() : null)
                .walletAddressTruncated(AdminUserService.truncateAddress(user.getWalletAddress()))
                .walletLinked(linked)
                .blocking(!linked && WALLET_CRITICAL_ROLES.contains(role))
                .build();
    }

    // ─── Health ──────────────────────────────────────────────────────────────

    /**
     * Everything an operator should look at first. Each figure is a problem
     * count: zero is healthy, non-zero is a card the admin can click through
     * to the filtered list behind it.
     */
    @Transactional(readOnly = true)
    public SystemHealthResponseDTO getHealth() {
        LocalDateTime staleCutoff = LocalDateTime.now().minusDays(STALE_AFTER_DAYS);

        // LinkedHashMap over the enum's own order: the roles read top-down in
        // the platform's hierarchy rather than in hash order.
        Map<Role, Long> countsByRole = new LinkedHashMap<>();
        long totalUsers = 0;
        for (Role role : Role.values()) {
            long count = userRepository.countByRole_RoleName(role);
            countsByRole.put(role, count);
            totalUsers += count;
        }

        List<AreaCoverageResponseDTO> vacantAreas = adminAreaService.getCoverage().stream()
                // A deactivated area has no officer on purpose — counting it as
                // a vacancy would make retiring an area raise a warning.
                .filter(area -> Boolean.TRUE.equals(area.getIsVacant())
                        && Boolean.TRUE.equals(area.getIsActive()))
                .toList();

        return SystemHealthResponseDTO.builder()
                .userCountsByRole(countsByRole)
                .totalUsers(totalUsers)
                .unlinkedWallets(userRepository.countByWalletAddressIsNull())
                .unlinkedOfficerWallets(userRepository.countByRole_RoleNameAndWalletAddressIsNull(
                        Role.AGRARIAN_SERVICE_OFFICER))
                .unlinkedGovernmentAdminWallets(userRepository.countByRole_RoleNameAndWalletAddressIsNull(
                        Role.GOVERNMENT_ADMIN))
                .vacantAreaCount(vacantAreas.size())
                .vacantAreas(vacantAreas)
                .staleFertilizerRequests(fertilizerRequestRepository.countByStatusAndCreatedAtBefore(
                        RequestStatus.PENDING, staleCutoff))
                .staleMarketOrders(marketOrderRepository.countByStatusAndCreatedAtBefore(
                        OrderStatus.PENDING_CONFIRMATION, staleCutoff))
                .staleAfterDays(STALE_AFTER_DAYS)
                .disputedDistributions(distributionLogRepository.countByDisputedTrue())
                .bannedUsers(userRepository.countByIsBannedTrue())
                .build();
    }
}
