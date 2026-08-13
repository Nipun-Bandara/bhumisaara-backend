package com.bandits.bhumisaara.repository;

import com.bandits.bhumisaara.entity.MarketOrderEntity;
import com.bandits.bhumisaara.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface MarketOrderRepository extends JpaRepository<MarketOrderEntity, Long> {

    boolean existsByCreditTransferHash(String creditTransferHash);

    List<MarketOrderEntity> findByFarmerIdOrderByCreatedAtDesc(Long farmerId);

    List<MarketOrderEntity> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    List<MarketOrderEntity> findAllByOrderByCreatedAtDesc();

    /** Blocks deleting a listing that still has orders riding on it. */
    boolean existsByListingIdAndStatusIn(Long listingId, Collection<OrderStatus> statuses);

    /**
     * Credits a farmer has actually spent — only a completed order moved any.
     * A disputed order still moved its credits, so it counts too.
     */
    @Query("SELECT COALESCE(SUM(o.creditsUsed), 0) FROM MarketOrderEntity o "
            + "WHERE o.farmerId = :farmerId AND o.status IN "
            + "(com.bandits.bhumisaara.enums.OrderStatus.COMPLETED, com.bandits.bhumisaara.enums.OrderStatus.DISPUTED)")
    long sumCreditsSpentByFarmer(@Param("farmerId") Long farmerId);

    @Query("SELECT COALESCE(SUM(o.creditsUsed), 0) FROM MarketOrderEntity o "
            + "WHERE o.sellerId = :sellerId AND o.status IN "
            + "(com.bandits.bhumisaara.enums.OrderStatus.COMPLETED, com.bandits.bhumisaara.enums.OrderStatus.DISPUTED)")
    long sumCreditsEarnedBySeller(@Param("sellerId") Long sellerId);

    /** Nationally spent credits — the middle term of the reconciliation. */
    @Query("SELECT COALESCE(SUM(o.creditsUsed), 0) FROM MarketOrderEntity o "
            + "WHERE o.status IN "
            + "(com.bandits.bhumisaara.enums.OrderStatus.COMPLETED, com.bandits.bhumisaara.enums.OrderStatus.DISPUTED)")
    long sumAllCreditsSpent();

    long countBySellerIdAndStatusIn(Long sellerId, Collection<OrderStatus> statuses);

    // ─── Platform administration ─────────────────────────────────────────────

    long countByFarmerId(Long farmerId);

    long countBySellerId(Long sellerId);

    long countByFarmerIdAndStatusIn(Long farmerId, Collection<OrderStatus> statuses);

    /** Orders a seller never acted on — the operator's stale-queue warning. */
    long countByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime cutoff);
}
