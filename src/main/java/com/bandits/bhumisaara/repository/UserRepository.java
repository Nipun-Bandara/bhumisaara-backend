package com.bandits.bhumisaara.repository;

import com.bandits.bhumisaara.entity.UserEntity;
import com.bandits.bhumisaara.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    /** Callers must pass the lowercased address — the column is normalised on write. */
    Optional<UserEntity> findByWalletAddress(String walletAddress);

    boolean existsByEmail(String email);

    List<UserEntity> findByRole_RoleNameOrderByUsernameAsc(Role roleName);

    List<UserEntity> findByRole_RoleNameAndIsAssignedOrderByUsernameAsc(Role roleName, Boolean isAssigned);
}
