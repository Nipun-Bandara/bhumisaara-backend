package com.bandits.bhumisaara.repository;

import com.bandits.bhumisaara.entity.AdminAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Reads and inserts only.
 * <p>
 * {@code JpaRepository} inherits {@code delete*} and {@code save} — nothing in
 * the codebase may call them against this entity, and no endpoint exposes
 * them. See {@link AdminAuditLogEntity} for why.
 * <p>
 * The filter combination (actor, action, date range, any subset) is built with
 * {@link JpaSpecificationExecutor} rather than a JPQL query full of
 * {@code :param IS NULL} branches, which Postgres cannot infer parameter types
 * for.
 */
@Repository
public interface AdminAuditLogRepository
        extends JpaRepository<AdminAuditLogEntity, Long>, JpaSpecificationExecutor<AdminAuditLogEntity> {
}
