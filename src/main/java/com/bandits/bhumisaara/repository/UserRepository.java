package com.bandits.bhumisaara.repository;

import com.bandits.bhumisaara.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<UserEntity> findByIsAssignedTrue(Pageable pageable);

    Page<UserEntity> findByIsAssignedFalse(Pageable pageable);
}
