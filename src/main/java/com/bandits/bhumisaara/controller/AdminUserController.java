package com.bandits.bhumisaara.controller;

import com.bandits.bhumisaara.dto.request.AssignUserAreaRequestDTO;
import com.bandits.bhumisaara.dto.request.ChangeRoleRequestDTO;
import com.bandits.bhumisaara.dto.request.ResetPasswordRequestDTO;
import com.bandits.bhumisaara.dto.response.AdminUserDetailDTO;
import com.bandits.bhumisaara.dto.response.AdminUserSummaryDTO;
import com.bandits.bhumisaara.dto.response.PageResponseDTO;
import com.bandits.bhumisaara.enums.Role;
import com.bandits.bhumisaara.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Account administration, restricted to the platform operator.
 * <p>
 * Nothing here touches tokens, batches, credits, listings or orders — a
 * {@code SYSTEM_ADMIN} governs who may participate in the fertilizer system
 * and never participates in it. The acting admin is always taken from the JWT;
 * no endpoint accepts an actor id.
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private static final int MAX_PAGE_SIZE = 100;

    private final AdminUserService adminUserService;

    /** The user directory. Every filter is optional. */
    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<PageResponseDTO<AdminUserSummaryDTO>> getUsers(
            @RequestParam(value = "role", required = false) Role role,
            @RequestParam(value = "areaId", required = false) Long areaId,
            @RequestParam(value = "isBanned", required = false) Boolean isBanned,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "25") int size) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(adminUserService.getUsers(
                        role,
                        areaId,
                        isBanned,
                        search,
                        Math.max(0, page),
                        Math.min(Math.max(1, size), MAX_PAGE_SIZE)));
    }

    /** One account with its activity counts — what to read before acting. */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<AdminUserDetailDTO> getUser(@PathVariable Long userId) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(adminUserService.getUser(userId));
    }

    @PostMapping("/{userId}/ban")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<AdminUserDetailDTO> banUser(@PathVariable Long userId) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(adminUserService.banUser(userId));
    }

    @PostMapping("/{userId}/unban")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<AdminUserDetailDTO> unbanUser(@PathVariable Long userId) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(adminUserService.unbanUser(userId));
    }

    /** Sets a new password. The value is hashed on arrival and never returned. */
    @PostMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<AdminUserDetailDTO> resetPassword(
            @PathVariable Long userId,
            @Valid @RequestBody ResetPasswordRequestDTO request) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(adminUserService.resetPassword(userId, request));
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<AdminUserDetailDTO> changeRole(
            @PathVariable Long userId,
            @Valid @RequestBody ChangeRoleRequestDTO request) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(adminUserService.changeRole(userId, request));
    }

    /** Assigns, moves, or (with a null areaId) clears an officer's area. */
    @PatchMapping("/{userId}/area")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<AdminUserDetailDTO> assignArea(
            @PathVariable Long userId,
            @Valid @RequestBody AssignUserAreaRequestDTO request) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(adminUserService.assignArea(userId, request));
    }

    /**
     * Unlinks a wallet. There is no endpoint that <em>sets</em> one, and adding
     * one would let an operator redirect minted tokens — see
     * {@code AdminUserService.clearWallet}.
     */
    @DeleteMapping("/{userId}/wallet")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<AdminUserDetailDTO> clearWallet(@PathVariable Long userId) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(adminUserService.clearWallet(userId));
    }
}
