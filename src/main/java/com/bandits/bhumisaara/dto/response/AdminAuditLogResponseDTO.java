package com.bandits.bhumisaara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * One audit row, with the actor and target resolved to usernames so the log
 * reads without a second lookup per line.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuditLogResponseDTO {

    private Long logId;

    private Long actorUserId;

    /** Null if the acting account has since been deleted from the database. */
    private String actorUsername;

    private String action;

    private Long targetUserId;

    private String targetUsername;

    private String targetEntity;

    private String details;

    private LocalDateTime createdAt;
}
