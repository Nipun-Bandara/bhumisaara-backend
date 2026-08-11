package com.bandits.bhumisaara.controller;

import com.bandits.bhumisaara.dto.request.UpdateWalletAddressRequestDTO;
import com.bandits.bhumisaara.dto.response.WalletAddressResponseDTO;
import com.bandits.bhumisaara.service.UserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserAccountService userAccountService;

    // Every role connects a wallet (government mints, officers burn, farmers
    // collect), so these are open to any authenticated user — spelled out
    // rather than left blank, so a missing annotation always reads as a bug.

    @GetMapping("/me/wallet")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WalletAddressResponseDTO> getMyWalletAddress() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userAccountService.getMyWalletAddress());
    }

    @PatchMapping("/me/wallet")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WalletAddressResponseDTO> updateMyWalletAddress(
            @Valid @RequestBody UpdateWalletAddressRequestDTO request) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userAccountService.updateMyWalletAddress(request));
    }
}
