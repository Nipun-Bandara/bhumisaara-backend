package com.bandits.bhumisaara.repository;

import com.bandits.bhumisaara.entity.FertilizerBatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FertilizerBatchRepository extends JpaRepository<FertilizerBatchEntity, Long> {

    Optional<FertilizerBatchEntity> findByTokenId(String tokenId);

    Optional<FertilizerBatchEntity> findByTransactionHash(String transactionHash);

    Optional<FertilizerBatchEntity> findByTokenIdOrTransactionHash(String tokenId, String transactionHash);
}
