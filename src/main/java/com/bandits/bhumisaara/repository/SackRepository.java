package com.bandits.bhumisaara.repository;

import com.bandits.bhumisaara.entity.SackEntity;
import com.bandits.bhumisaara.enums.SackStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SackRepository extends JpaRepository<SackEntity, Long> {

    boolean existsBySerial(String serial);

    Optional<SackEntity> findBySerial(String serial);

    List<SackEntity> findBySerialIn(Collection<String> serials);

    List<SackEntity> findByBatchIdOrderBySackIdAsc(Long batchId);

    List<SackEntity> findByHeldByUserIdAndStatusOrderBySackIdAsc(Long heldByUserId, SackStatus status);

    long countByBatchId(Long batchId);
}
