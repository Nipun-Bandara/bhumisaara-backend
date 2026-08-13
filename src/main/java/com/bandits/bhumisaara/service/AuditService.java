package com.bandits.bhumisaara.service;

import com.bandits.bhumisaara.entity.AdminAuditLogEntity;
import com.bandits.bhumisaara.entity.UserEntity;
import com.bandits.bhumisaara.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The single writer of {@code admin_audit_logs}.
 * <p>
 * Every privileged administrative action calls {@link #record} from inside its
 * own {@code @Transactional} method, which is why this one is
 * {@link Propagation#MANDATORY}: it refuses to run without a caller
 * transaction to join. That turns "remember to log in the same transaction"
 * from a convention into something the container enforces — the action and its
 * audit row commit together, or neither does.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    /** The maximum {@code details} length, matching the column. */
    private static final int MAX_DETAILS = 500;

    private final AdminAuditLogRepository auditLogRepository;

    /**
     * Actions this service records. Constants rather than free text, so the
     * audit-log filter can offer a fixed list and a typo can't create a new
     * kind of action that nobody ever searches for.
     */
    public static final class Action {
        public static final String USER_BANNED = "USER_BANNED";
        public static final String USER_UNBANNED = "USER_UNBANNED";
        public static final String USER_PASSWORD_RESET = "USER_PASSWORD_RESET";
        public static final String USER_ROLE_CHANGED = "USER_ROLE_CHANGED";
        public static final String USER_AREA_ASSIGNED = "USER_AREA_ASSIGNED";
        public static final String USER_AREA_CLEARED = "USER_AREA_CLEARED";
        public static final String USER_WALLET_CLEARED = "USER_WALLET_CLEARED";
        public static final String AREA_CREATED = "AREA_CREATED";
        public static final String AREA_UPDATED = "AREA_UPDATED";
        public static final String AREA_DEACTIVATED = "AREA_DEACTIVATED";
        public static final String AREA_REACTIVATED = "AREA_REACTIVATED";

        private Action() {
        }
    }

    /**
     * Writes one audit row.
     *
     * @param actor        the acting admin, resolved from the JWT by the caller
     * @param action       one of {@link Action}
     * @param targetUserId the user acted upon, or null
     * @param targetEntity a non-user target such as {@code "area:7"}, or null
     * @param details      what changed; never a password, hash or secret
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(UserEntity actor,
                       String action,
                       Long targetUserId,
                       String targetEntity,
                       String details) {
        auditLogRepository.save(AdminAuditLogEntity.builder()
                .actorUserId(actor.getUserId())
                .action(action)
                .targetUserId(targetUserId)
                .targetEntity(targetEntity)
                .details(truncate(details))
                .build());
    }

    private String truncate(String details) {
        if (details == null || details.length() <= MAX_DETAILS) {
            return details;
        }
        return details.substring(0, MAX_DETAILS - 1) + "…";
    }
}
