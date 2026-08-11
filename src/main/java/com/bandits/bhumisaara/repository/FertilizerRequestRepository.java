package com.bandits.bhumisaara.repository;

import com.bandits.bhumisaara.entity.FertilizerRequestEntity;
import com.bandits.bhumisaara.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FertilizerRequestRepository extends JpaRepository<FertilizerRequestEntity, Long> {

    List<FertilizerRequestEntity> findByFarmer_UserIdOrderByCreatedAtDesc(Long farmerId);

    /**
     * The officer review queue: requests raised by farmers in the officer's own
     * area. Oldest first — a work queue should be FIFO.
     */
    List<FertilizerRequestEntity> findByStatusAndFarmer_Area_AreaIdOrderByCreatedAtAsc(
            RequestStatus status, Long areaId);

    /**
     * The officer's area history. Newest first, which is the opposite of the
     * queue above — history reads most-recent-first, a queue reads oldest-first.
     */
    List<FertilizerRequestEntity> findByFarmer_Area_AreaIdOrderByCreatedAtDesc(Long areaId);

    List<FertilizerRequestEntity> findByStatusAndFarmer_Area_AreaIdOrderByCreatedAtDesc(
            RequestStatus status, Long areaId);

    boolean existsByFarmer_UserIdAndSeasonAndFertilizerTypeAndStatus(
            Long farmerId, String season, String fertilizerType, RequestStatus status);
}
