package com.bandits.bhumisaara.controller;

import com.bandits.bhumisaara.dto.response.AdminAuditLogResponseDTO;
import com.bandits.bhumisaara.dto.response.PageResponseDTO;
import com.bandits.bhumisaara.service.AdminAuditQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * The administrative audit trail.
 * <p>
 * <strong>Read-only by design.</strong> There is no POST, PATCH or DELETE here
 * and there must never be: an audit log an administrator can edit records
 * nothing. Rows are written only by {@code AuditService}, inside the
 * transaction of the action they describe.
 */
@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private static final int MAX_PAGE_SIZE = 100;

    private final AdminAuditQueryService adminAuditQueryService;

    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<PageResponseDTO<AdminAuditLogResponseDTO>> getAuditLogs(
            @RequestParam(value = "actorUserId", required = false) Long actorUserId,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "25") int size) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(adminAuditQueryService.getAuditLogs(
                        actorUserId,
                        action,
                        from,
                        to,
                        Math.max(0, page),
                        Math.min(Math.max(1, size), MAX_PAGE_SIZE)));
    }
}
