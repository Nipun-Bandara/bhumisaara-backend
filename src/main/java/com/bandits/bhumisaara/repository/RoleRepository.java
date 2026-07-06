package com.bandits.bhumisaara.repository;

import com.bandits.bhumisaara.entity.RoleEntity;
import com.bandits.bhumisaara.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByRoleName(Role roleName);
}
