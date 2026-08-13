package com.bandits.bhumisaara.entity;

import com.bandits.bhumisaara.enums.TokenType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

/**
 * One season's subsidy credits, minted to one farmer's wallet.
 * <p>
 * Unlike a fertilizer batch this is backed by nothing physical — the credits
 * are a claim on the treasury, created when the government funds the season's
 * budget. They leave circulation only when a seller redeems them
 * ({@code redemption_claims}), never when goods move.
 */
@Entity
@Table(
        name = "subsidy_credit_issuances",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_issuance_farmer_season",
                columnNames = {"farmer_id", "season"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubsidyCreditIssuanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issuance_id")
    private Long issuanceId;

    @Column(name = "farmer_id", nullable = false)
    private Long farmerId;

    /** e.g. {@code "Maha 2025/2026"}, matching `fertilizer_requests.season`. */
    @Column(name = "season", nullable = false, length = 50)
    private String season;

    @Column(name = "credits_kg", nullable = false)
    private Integer creditsKg;

    /**
     * The ERC-1155 id the season's credits live under, stored as `String` per
     * the repo-wide tokenId convention.
     * <p>
     * Not in the original column list but load-bearing: every farmer issued
     * credits for a given season shares one token id, which is what makes a
     * season's credits fungible between farmers and what
     * {@code balanceOf(wallet, tokenId)} needs in order to read a balance at
     * all. The service refuses an issuance whose token id disagrees with the
     * one the season already uses.
     */
    @Column(name = "token_id", nullable = false)
    private String tokenId;

    /** Always SUBSIDY_CREDIT here; present so a join can never confuse the two. */
    @Builder.Default
    @ColumnDefault("'SUBSIDY_CREDIT'")
    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 20)
    private TokenType tokenType = TokenType.SUBSIDY_CREDIT;

    @Column(name = "transaction_hash", nullable = false, unique = true)
    private String transactionHash;

    /** The issuing government admin, taken from the JWT. */
    @Column(name = "issued_by_user_id", nullable = false)
    private Long issuedByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
