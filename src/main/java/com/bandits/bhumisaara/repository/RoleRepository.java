package com.bhumisaara.repository;

import com.bhumisaara.entity.RoleEntity;
import com.bhumisaara.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByRoleName(Role roleName);
}
