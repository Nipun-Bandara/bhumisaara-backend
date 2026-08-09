package com.bandits.bhumisaara.service;

import com.bandits.bhumisaara.dto.request.HandoverRequestDTO;
import com.bandits.bhumisaara.dto.response.DistributionResponseDTO;
import com.bandits.bhumisaara.entity.DistributionLogEntity;
import com.bandits.bhumisaara.entity.FertilizerBatchEntity;
import com.bandits.bhumisaara.repository.DistributionLogRepository;
import com.bandits.bhumisaara.repository.FertilizerBatchRepository;
import com.bandits.bhumisaara.repository.UserQuotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DistributionService {

    private final DistributionLogRepository distributionLogRepository;
    private final FertilizerBatchRepository batchRepository;
    private final UserQuotaRepository quotaRepository;

    @Transactional
    public DistributionResponseDTO recordHandover(HandoverRequestDTO request) {
        if (distributionLogRepository.existsByBurnTransactionHash(request.getBurnTransactionHash())) {
            throw new IllegalStateException(
                    "A distribution with burn_transaction_hash '" + request.getBurnTransactionHash() + "' already exists");
        }

        FertilizerBatchEntity batch = batchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Fertilizer batch not found with id: " + request.getBatchId()));

        quotaRepository.findByFarmerIdAndFertilizerType(request.getFarmerId(), batch.getFertilizerType())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No quota allocated for farmer " + request.getFarmerId()
                                + " and fertilizer type '" + batch.getFertilizerType() + "'"));

        int batchRowsUpdated = batchRepository.deductVolumeIfSufficient(
                request.getBatchId(), request.getAmountDispensedKg());
        if (batchRowsUpdated == 0) {
            throw new IllegalStateException(
                    "Insufficient remaining volume in batch " + request.getBatchId()
                            + " for requested " + request.getAmountDispensedKg() + " kg");
        }

        int quotaRowsUpdated = quotaRepository.deductQuotaIfSufficient(
                request.getFarmerId(), batch.getFertilizerType(), request.getAmountDispensedKg());
        if (quotaRowsUpdated == 0) {
            throw new IllegalStateException(
                    "Insufficient remaining quota for farmer " + request.getFarmerId()
                            + " and fertilizer type '" + batch.getFertilizerType() + "'");
        }

        DistributionLogEntity entity = DistributionLogEntity.builder()
                .tokenId(request.getTokenId())
                .batchId(request.getBatchId())
                .farmerId(request.getFarmerId())
                .officerId(request.getOfficerId())
                .amountDispensedKg(request.getAmountDispensedKg())
                .burnTransactionHash(request.getBurnTransactionHash())
                .build();

        DistributionLogEntity saved = distributionLogRepository.save(entity);
        return mapToDTO(saved);
    }

    private DistributionResponseDTO mapToDTO(DistributionLogEntity entity) {
        return DistributionResponseDTO.builder()
                .distributionId(entity.getDistributionId())
                .tokenId(entity.getTokenId())
                .batchId(entity.getBatchId())
                .farmerId(entity.getFarmerId())
                .officerId(entity.getOfficerId())
                .amountDispensedKg(entity.getAmountDispensedKg())
                .burnTransactionHash(entity.getBurnTransactionHash())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
