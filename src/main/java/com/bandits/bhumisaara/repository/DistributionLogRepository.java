package com.bandits.bhumisaara.repository;

import com.bandits.bhumisaara.entity.DistributionLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistributionLogRepository extends JpaRepository<DistributionLogEntity, Long> {

    boolean existsByBurnTransactionHash(String burnTransactionHash);

    /** History reads newest-first for every audience. */
    List<DistributionLogEntity> findAllByOrderByCreatedAtDesc();

    List<DistributionLogEntity> findByOfficerIdOrderByCreatedAtDesc(Long officerId);

    List<DistributionLogEntity> findByFarmerIdOrderByCreatedAtDesc(Long farmerId);

    // ─── Platform administration ─────────────────────────────────────────────

    long countByFarmerId(Long farmerId);

    long countByOfficerId(Long officerId);

    /** Handovers a farmer says never reached them — each one needs a human. */
    long countByDisputedTrue();
}
