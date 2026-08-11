package com.bandits.bhumisaara.service;

import com.bandits.bhumisaara.dto.request.UpdateWalletAddressRequestDTO;
import com.bandits.bhumisaara.dto.response.WalletAddressResponseDTO;
import com.bandits.bhumisaara.entity.UserEntity;
import com.bandits.bhumisaara.repository.UserRepository;
import com.bandits.bhumisaara.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public WalletAddressResponseDTO getMyWalletAddress() {
        return mapToDTO(currentUserProvider.require());
    }

    /**
     * Links the connected wallet to the authenticated user. Called by the frontend
     * the first time an address is seen; re-sending the same address is a no-op, and
     * connecting a different wallet replaces the stored one.
     */
    @Transactional
    public WalletAddressResponseDTO updateMyWalletAddress(UpdateWalletAddressRequestDTO request) {
        UserEntity user = currentUserProvider.require();

        // Normalise here as well as in UserEntity's lifecycle callback, so the
        // uniqueness lookup below compares like with like.
        String walletAddress = request.getWalletAddress().trim().toLowerCase();

        userRepository.findByWalletAddress(walletAddress).ifPresent(owner -> {
            if (!owner.getUserId().equals(user.getUserId())) {
                throw new IllegalStateException(
                        "That wallet address is already linked to another account");
            }
        });

        user.setWalletAddress(walletAddress);

        return mapToDTO(userRepository.save(user));
    }

    private WalletAddressResponseDTO mapToDTO(UserEntity user) {
        return WalletAddressResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .walletAddress(user.getWalletAddress())
                .build();
    }
}
