package com.bandits.bhumisaara.controller;

import com.bandits.bhumisaara.dto.response.AdminWalletStatusDTO;
import com.bandits.bhumisaara.dto.response.SystemHealthResponseDTO;
import com.bandits.bhumisaara.service.AdminPlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only platform oversight: who has a wallet, and what needs attention.
 * <p>
 * The clearing endpoint that pairs with the wallet list lives on
 * {@code AdminUserController} ({@code DELETE /admin/users/{id}/wallet}),
 * because it acts on a user rather than on the roster.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPlatformController {

    private final AdminPlatformService adminPlatformService;

    /**
     * Wallet link status for every account.
     *
     * @param unlinkedOnly narrows the list to accounts with no wallet yet
     */
    @GetMapping("/wallets")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<AdminWalletStatusDTO>> getWallets(
            @RequestParam(value = "unlinkedOnly", defaultValue = "false") boolean unlinkedOnly) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(adminPlatformService.getWalletStatuses(unlinkedOnly));
    }

    /** The operational warning summary. Every count reads healthy at zero. */
    @GetMapping("/health")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<SystemHealthResponseDTO> getHealth() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(adminPlatformService.getHealth());
    }
}
