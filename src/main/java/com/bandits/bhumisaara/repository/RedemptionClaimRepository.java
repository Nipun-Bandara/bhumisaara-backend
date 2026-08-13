package com.bandits.bhumisaara.repository;

import com.bandits.bhumisaara.entity.RedemptionClaimEntity;
import com.bandits.bhumisaara.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RedemptionClaimRepository extends JpaRepository<RedemptionClaimEntity, Long> {

    boolean existsByBurnTransactionHash(String burnTransactionHash);

    List<RedemptionClaimEntity> findBySellerIdOrderBySubmittedAtDesc(Long sellerId);

    List<RedemptionClaimEntity> findAllByOrderBySubmittedAtDesc();

    List<RedemptionClaimEntity> findByStatusInOrderBySubmittedAtAsc(Collection<ClaimStatus> statuses);

    /**
     * Credits the seller has already committed to a claim that hasn't been
     * refused. A seller must not be able to claim the same credits twice by
     * filing two claims before either is processed.
     */
    @Query("SELECT COALESCE(SUM(c.creditsClaimed), 0) FROM RedemptionClaimEntity c "
            + "WHERE c.sellerId = :sellerId AND c.status <> com.bandits.bhumisaara.enums.ClaimStatus.REJECTED")
    long sumOpenOrSettledCreditsBySeller(@Param("sellerId") Long sellerId);

    /** Only a PAID claim actually burned anything. */
    @Query("SELECT COALESCE(SUM(c.creditsClaimed), 0) FROM RedemptionClaimEntity c "
            + "WHERE c.sellerId = :sellerId AND c.status = com.bandits.bhumisaara.enums.ClaimStatus.PAID")
    long sumRedeemedCreditsBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT COALESCE(SUM(c.creditsClaimed), 0) FROM RedemptionClaimEntity c "
            + "WHERE c.status = com.bandits.bhumisaara.enums.ClaimStatus.PAID")
    long sumAllRedeemedCredits();
}
