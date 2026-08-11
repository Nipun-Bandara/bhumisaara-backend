package com.bandits.bhumisaara.repository;

import com.bandits.bhumisaara.entity.UserQuotaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserQuotaRepository extends JpaRepository<UserQuotaEntity, Long> {

    Optional<UserQuotaEntity> findByFarmerIdAndFertilizerType(Long farmerId, String fertilizerType);

    @Modifying
    @Query("UPDATE UserQuotaEntity q SET q.remainingKg = q.remainingKg - :amount, q.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE q.farmerId = :farmerId AND q.fertilizerType = :fertilizerType AND q.remainingKg >= :amount")
    int deductQuotaIfSufficient(@Param("farmerId") Long farmerId,
                                 @Param("fertilizerType") String fertilizerType,
                                 @Param("amount") Integer amount);
}
