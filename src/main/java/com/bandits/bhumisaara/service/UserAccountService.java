package com.bandits.bhumisaara.service;

import com.bandits.bhumisaara.dto.request.UpdateProfileRequestDTO;
import com.bandits.bhumisaara.dto.request.UpdateWalletAddressRequestDTO;
import com.bandits.bhumisaara.dto.response.ProfileResponseDTO;
import com.bandits.bhumisaara.dto.response.WalletAddressResponseDTO;
import com.bandits.bhumisaara.entity.AreaEntity;
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

    /** The signed-in user's own profile, whatever their role. */
    @Transactional(readOnly = true)
    public ProfileResponseDTO getMyProfile() {
        return mapToProfileDTO(currentUserProvider.require());
    }

    /**
     * Saves the profile details of the signed-in user. Always their own record —
     * there is no user id in the payload, so this cannot edit anyone else.
     * <p>
     * Username, email and role are identity and stay untouched; the wallet and
     * the area have their own endpoints and are not cleared by a profile save.
     */
    @Transactional
    public ProfileResponseDTO updateMyProfile(UpdateProfileRequestDTO request) {
        UserEntity user = currentUserProvider.require();

        user.setFullName(request.getFullName().trim());
        user.setAddress(request.getAddress().trim());
        user.setContactNumber(request.getContactNumber().trim());

        return mapToProfileDTO(userRepository.save(user));
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

    private ProfileResponseDTO mapToProfileDTO(UserEntity user) {
        AreaEntity area = user.getArea();

        return ProfileResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getRoleName() : null)
                .fullName(user.getFullName())
                .address(user.getAddress())
                .contactNumber(user.getContactNumber())
                .walletAddress(user.getWalletAddress())
                .areaId(area != null ? area.getAreaId() : null)
                .areaName(area != null ? area.getAreaName() : null)
                .district(area != null ? area.getDistrict() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private WalletAddressResponseDTO mapToDTO(UserEntity user) {
        return WalletAddressResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .walletAddress(user.getWalletAddress())
                .build();
    }
}
