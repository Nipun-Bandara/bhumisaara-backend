package com.bandits.bhumisaara.service;

import com.bandits.bhumisaara.dto.request.BatchRequestDTO;
import com.bandits.bhumisaara.dto.response.BatchResponseDTO;
import com.bandits.bhumisaara.entity.FertilizerBatchEntity;
import com.bandits.bhumisaara.repository.FertilizerBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FertilizerBatchService {

    private final FertilizerBatchRepository batchRepository;

    @Transactional
    public BatchResponseDTO createBatch(BatchRequestDTO request) {
        batchRepository.findByTokenIdOrTransactionHash(request.getTokenId(), request.getTransactionHash())
                .ifPresent(existing -> {
                    if (request.getTokenId().equals(existing.getTokenId())) {
                        throw new IllegalStateException("A batch with token_id '" + request.getTokenId() + "' already exists");
                    }
                    throw new IllegalStateException("A batch with transaction_hash '" + request.getTransactionHash() + "' already exists");
                });

        FertilizerBatchEntity entity = FertilizerBatchEntity.builder()
                .tokenId(request.getTokenId())
                .transactionHash(request.getTransactionHash())
                .importerName(request.getImporterName())
                .fertilizerType(request.getFertilizerType())
                .volumeKg(request.getVolumeKg())
                .mintedByUserId(request.getMintedByUserId())
                .build();

        FertilizerBatchEntity savedEntity = batchRepository.save(entity);
        return mapToDTO(savedEntity);
    }

    @Transactional(readOnly = true)
    public List<BatchResponseDTO> getAllBatches() {
        return batchRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BatchResponseDTO getBatchById(Long batchId) {
        FertilizerBatchEntity entity = batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Fertilizer batch not found with id: " + batchId));
        return mapToDTO(entity);
    }

    @Transactional(readOnly = true)
    public BatchResponseDTO getBatchByTokenId(String tokenId) {
        FertilizerBatchEntity entity = batchRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Fertilizer batch not found with token_id: " + tokenId));
        return mapToDTO(entity);
    }

    private BatchResponseDTO mapToDTO(FertilizerBatchEntity entity) {
        return BatchResponseDTO.builder()
                .batchId(entity.getBatchId())
                .tokenId(entity.getTokenId())
                .transactionHash(entity.getTransactionHash())
                .importerName(entity.getImporterName())
                .fertilizerType(entity.getFertilizerType())
                .volumeKg(entity.getVolumeKg())
                .mintedByUserId(entity.getMintedByUserId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
