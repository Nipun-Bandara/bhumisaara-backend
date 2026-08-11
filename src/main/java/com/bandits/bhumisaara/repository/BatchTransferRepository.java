package com.bandits.bhumisaara.repository;

import com.bandits.bhumisaara.entity.BatchTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchTransferRepository extends JpaRepository<BatchTransferEntity, Long> {

    boolean existsByTransactionHash(String transactionHash);

    List<BatchTransferEntity> findAllByOrderByCreatedAtDesc();

    /**
     * How much has already been moved to each officer, per fertilizer type.
     * The type lives on the batch, so this is an ad-hoc join — {@code batch_id}
     * is a plain column rather than an association on the transfer entity.
     */
    @Query("SELECT t.toOfficerId AS officerId, "
            + "b.fertilizerType AS fertilizerType, "
            + "SUM(t.amountKg) AS transferredKg "
            + "FROM BatchTransferEntity t "
            + "JOIN FertilizerBatchEntity b ON b.batchId = t.batchId "
            + "GROUP BY t.toOfficerId, b.fertilizerType")
    List<OfficerTransferAggregate> sumTransferredKgByOfficerAndType();

    /** Projection for {@link #sumTransferredKgByOfficerAndType()}. */
    interface OfficerTransferAggregate {
        Long getOfficerId();

        String getFertilizerType();

        Long getTransferredKg();
    }
}
