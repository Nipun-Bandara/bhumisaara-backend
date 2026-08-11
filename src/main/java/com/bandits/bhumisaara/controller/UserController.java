package com.bandits.bhumisaara.controller;

import com.bandits.bhumisaara.dto.request.UpdateWalletAddressRequestDTO;
import com.bandits.bhumisaara.dto.response.WalletAddressResponseDTO;
import com.bandits.bhumisaara.service.UserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserAccountService userAccountService;

    // No @PreAuthorize: every role connects a wallet (government mints, officers
    // burn, farmers collect), so this is open to any authenticated user.

    @GetMapping("/me/wallet")
    public ResponseEntity<WalletAddressResponseDTO> getMyWalletAddress() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userAccountService.getMyWalletAddress());
    }

    @PatchMapping("/me/wallet")
    public ResponseEntity<WalletAddressResponseDTO> updateMyWalletAddress(
            @Valid @RequestBody UpdateWalletAddressRequestDTO request) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userAccountService.updateMyWalletAddress(request));
    }
}
