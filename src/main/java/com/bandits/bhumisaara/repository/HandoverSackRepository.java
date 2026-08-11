package com.bandits.bhumisaara.repository;

import com.bandits.bhumisaara.entity.HandoverSackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface HandoverSackRepository extends JpaRepository<HandoverSackEntity, Long> {

    /** The consume-once check: has this physical sack already backed a handover? */
    boolean existsBySackSerial(String sackSerial);

    List<HandoverSackEntity> findBySackSerialIn(Collection<String> sackSerials);

    List<HandoverSackEntity> findByDistributionIdInOrderByIdAsc(Collection<Long> distributionIds);
}
