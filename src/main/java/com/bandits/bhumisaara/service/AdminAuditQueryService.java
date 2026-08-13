package com.bandits.bhumisaara.service;

import com.bandits.bhumisaara.dto.response.AdminAuditLogResponseDTO;
import com.bandits.bhumisaara.dto.response.PageResponseDTO;
import com.bandits.bhumisaara.entity.AdminAuditLogEntity;
import com.bandits.bhumisaara.entity.UserEntity;
import com.bandits.bhumisaara.repository.AdminAuditLogRepository;
import com.bandits.bhumisaara.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reads {@code admin_audit_logs}. Deliberately separate from
 * {@link AuditService}, which only writes: keeping the query side out of the
 * writer means no code path can reach a mutation while "just" reading the log.
 */
@Service
@RequiredArgsConstructor
public class AdminAuditQueryService {

    private final AdminAuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * The audit trail, newest first.
     *
     * @param actorUserId only actions by this admin, or null for all
     * @param action      one of {@code AuditService.Action}, or null for all
     * @param from        inclusive lower bound on {@code created_at}, or null
     * @param to          inclusive upper bound on {@code created_at}, or null
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<AdminAuditLogResponseDTO> getAuditLogs(Long actorUserId,
                                                                  String action,
                                                                  LocalDateTime from,
                                                                  LocalDateTime to,
                                                                  int page,
                                                                  int size) {
        List<Specification<AdminAuditLogEntity>> filters = new ArrayList<>();

        if (actorUserId != null) {
            filters.add((root, query, cb) -> cb.equal(root.get("actorUserId"), actorUserId));
        }
        if (action != null && !action.isBlank()) {
            filters.add((root, query, cb) -> cb.equal(root.get("action"), action.trim()));
        }
        if (from != null) {
            filters.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            filters.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }

        // A tautology rather than null when no filter is set — see the same
        // note in AdminUserService.getUsers.
        Specification<AdminAuditLogEntity> spec = filters.stream()
                .reduce(Specification::and)
                .orElse((root, query, cb) -> cb.conjunction());

        Page<AdminAuditLogEntity> results = auditLogRepository.findAll(
                spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "logId")));

        // One lookup for every user named on the page, rather than two per row.
        Map<Long, String> usernames = resolveUsernames(results.getContent());

        return PageResponseDTO.from(results, entry -> mapToDTO(entry, usernames));
    }

    private Map<Long, String> resolveUsernames(List<AdminAuditLogEntity> entries) {
        List<Long> userIds = entries.stream()
                .flatMap(entry -> Stream.of(entry.getActorUserId(), entry.getTargetUserId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (userIds.isEmpty()) {
            return Map.of();
        }

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getUserId, UserEntity::getUsername));
    }

    private AdminAuditLogResponseDTO mapToDTO(AdminAuditLogEntity entry, Map<Long, String> usernames) {
        Function<Long, String> nameOf = id -> id == null ? null : usernames.get(id);

        return AdminAuditLogResponseDTO.builder()
                .logId(entry.getLogId())
                .actorUserId(entry.getActorUserId())
                .actorUsername(nameOf.apply(entry.getActorUserId()))
                .action(entry.getAction())
                .targetUserId(entry.getTargetUserId())
                .targetUsername(nameOf.apply(entry.getTargetUserId()))
                .targetEntity(entry.getTargetEntity())
                .details(entry.getDetails())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
