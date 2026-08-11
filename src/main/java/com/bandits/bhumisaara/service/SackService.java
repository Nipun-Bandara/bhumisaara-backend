package com.bandits.bhumisaara.service;

import com.bandits.bhumisaara.dto.response.SackResponseDTO;
import com.bandits.bhumisaara.entity.FertilizerBatchEntity;
import com.bandits.bhumisaara.entity.SackEntity;
import com.bandits.bhumisaara.entity.UserEntity;
import com.bandits.bhumisaara.enums.SackStatus;
import com.bandits.bhumisaara.repository.FertilizerBatchRepository;
import com.bandits.bhumisaara.repository.SackRepository;
import com.bandits.bhumisaara.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Splits a minted batch into physically labelled sacks.
 * <p>
 * Serials are what the officer scans in the field, so they must be
 * unpredictable: a sequential serial would let anyone print a label that scans
 * as a genuine sack they never received.
 */
@Service
@RequiredArgsConstructor
public class SackService {

    /** Standard sack size; the last sack of a batch carries the remainder. */
    public static final int SACK_WEIGHT_KG = 50;

    /**
     * Refuses to explode a single mint into an unbounded number of rows —
     * 5 000 sacks is 250 tonnes, well past anything a demo batch should be.
     */
    private static final int MAX_SACKS_PER_BATCH = 5_000;

    private static final char[] SERIAL_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final int SERIAL_LENGTH = 12;
    private static final int SERIAL_GROUP_SIZE = 4;
    private static final int SERIAL_ATTEMPTS = 12;

    private final SackRepository sackRepository;
    private final FertilizerBatchRepository batchRepository;
    private final CurrentUserProvider currentUserProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Creates the sacks for a freshly saved batch. Called from within the batch
     * transaction so a batch is never persisted without its sacks.
     *
     * @return the generated sacks, in label order.
     */
    public List<SackEntity> generateSacksForBatch(FertilizerBatchEntity batch) {
        List<Integer> weights = splitIntoSackWeights(batch.getVolumeKg());

        if (weights.size() > MAX_SACKS_PER_BATCH) {
            throw new IllegalArgumentException(
                    "volume_kg " + batch.getVolumeKg() + " would produce " + weights.size()
                            + " sacks, above the " + MAX_SACKS_PER_BATCH + " limit for a single batch");
        }

        // Serials are unique across the whole table, so candidates must be
        // checked against the ones generated earlier in this same call too —
        // they aren't in the database yet.
        Set<String> reserved = new HashSet<>();
        List<SackEntity> sacks = new ArrayList<>(weights.size());

        for (Integer weightKg : weights) {
            sacks.add(SackEntity.builder()
                    .batchId(batch.getBatchId())
                    .serial(generateUniqueSerial(reserved))
                    .weightKg(weightKg)
                    .status(SackStatus.AT_CENTRAL)
                    .heldByUserId(batch.getMintedByUserId())
                    .build());
        }

        return sackRepository.saveAll(sacks);
    }

    /**
     * Generates the sacks for a batch minted before sacks existed. Idempotent:
     * a batch that already has sacks is returned untouched rather than doubled.
     */
    @Transactional
    public List<SackResponseDTO> backfillSacksForBatch(Long batchId) {
        FertilizerBatchEntity batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Fertilizer batch not found with id: " + batchId));

        if (sackRepository.countByBatchId(batchId) > 0) {
            return getSacksForBatch(batchId);
        }

        return generateSacksForBatch(batch).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SackResponseDTO> getSacksForBatch(Long batchId) {
        return enrichWithBatch(sackRepository.findByBatchIdOrderBySackIdAsc(batchId));
    }

    /**
     * The sacks physically in the calling officer's custody — their real local
     * inventory, as opposed to what the ministry has minted nationally.
     */
    @Transactional(readOnly = true)
    public List<SackResponseDTO> getMySacks() {
        UserEntity officer = currentUserProvider.require();

        return enrichWithBatch(sackRepository.findByHeldByUserIdAndStatusOrderBySackIdAsc(
                officer.getUserId(), SackStatus.WITH_OFFICER));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /** Full 50kg sacks, with the remainder as a final short sack. */
    private List<Integer> splitIntoSackWeights(Integer volumeKg) {
        if (volumeKg == null || volumeKg <= 0) {
            throw new IllegalArgumentException("volume_kg must be greater than zero to produce sacks");
        }

        List<Integer> weights = new ArrayList<>();
        int remaining = volumeKg;

        while (remaining >= SACK_WEIGHT_KG) {
            weights.add(SACK_WEIGHT_KG);
            remaining -= SACK_WEIGHT_KG;
        }
        if (remaining > 0) {
            weights.add(remaining);
        }

        return weights;
    }

    /**
     * A random 12-character serial rendered as {@code XXXX-XXXX-XXXX}.
     * <p>
     * Collisions are vanishingly unlikely at 36^12, but a taken serial is
     * simply re-rolled rather than allowed to hit the unique constraint — a
     * constraint violation would poison the surrounding transaction and lose
     * the whole batch.
     */
    private String generateUniqueSerial(Set<String> reserved) {
        for (int attempt = 0; attempt < SERIAL_ATTEMPTS; attempt++) {
            String candidate = randomSerial();

            if (!reserved.contains(candidate) && !sackRepository.existsBySerial(candidate)) {
                reserved.add(candidate);
                return candidate;
            }
        }

        throw new IllegalStateException(
                "Could not generate a unique sack serial after " + SERIAL_ATTEMPTS + " attempts");
    }

    private String randomSerial() {
        StringBuilder serial = new StringBuilder(SERIAL_LENGTH + 2);

        for (int i = 0; i < SERIAL_LENGTH; i++) {
            if (i > 0 && i % SERIAL_GROUP_SIZE == 0) {
                serial.append('-');
            }
            serial.append(SERIAL_ALPHABET[secureRandom.nextInt(SERIAL_ALPHABET.length)]);
        }

        return serial.toString();
    }

    /** Looks the batches up once for the whole list rather than per sack. */
    private List<SackResponseDTO> enrichWithBatch(List<SackEntity> sacks) {
        if (sacks.isEmpty()) {
            return List.of();
        }

        Map<Long, FertilizerBatchEntity> batches = batchRepository
                .findAllById(sacks.stream().map(SackEntity::getBatchId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(FertilizerBatchEntity::getBatchId, batch -> batch));

        return sacks.stream()
                .map(sack -> {
                    SackResponseDTO dto = mapToDTO(sack);
                    FertilizerBatchEntity batch = batches.get(sack.getBatchId());
                    if (batch != null) {
                        dto.setFertilizerType(batch.getFertilizerType());
                        dto.setTokenId(batch.getTokenId());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private SackResponseDTO mapToDTO(SackEntity entity) {
        return SackResponseDTO.builder()
                .sackId(entity.getSackId())
                .batchId(entity.getBatchId())
                .serial(entity.getSerial())
                .weightKg(entity.getWeightKg())
                .status(entity.getStatus())
                .heldByUserId(entity.getHeldByUserId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
