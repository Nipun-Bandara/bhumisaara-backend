package com.bandits.bhumisaara.repository;

import com.bandits.bhumisaara.entity.DistributionLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistributionLogRepository extends JpaRepository<DistributionLogEntity, Long> {

    boolean existsByBurnTransactionHash(String burnTransactionHash);
}
