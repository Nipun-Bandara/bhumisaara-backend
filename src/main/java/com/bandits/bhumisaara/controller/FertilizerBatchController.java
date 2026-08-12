package com.bandits.bhumisaara.controller;

import com.bandits.bhumisaara.dto.request.BatchRequestDTO;
import com.bandits.bhumisaara.dto.response.BatchResponseDTO;
import com.bandits.bhumisaara.service.FertilizerBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/batches")
@RequiredArgsConstructor
public class FertilizerBatchController {

    /**
     * Only the government admin mints, but officers read the batch list to pick
     * the stock they dispense from, so reads stay open to them.
     */
    private static final String BATCH_READERS =
            "hasAnyRole('GOVERNMENT_ADMIN', 'SYSTEM_ADMIN', 'AGRARIAN_SERVICE_OFFICER')";

    private final FertilizerBatchService batchService;

    @PostMapping
    @PreAuthorize("hasRole('GOVERNMENT_ADMIN')")
    public ResponseEntity<BatchResponseDTO> createBatch(@Valid @RequestBody BatchRequestDTO request) {
        BatchResponseDTO response = batchService.createBatch(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @GetMapping
    @PreAuthorize(BATCH_READERS)
    public ResponseEntity<List<BatchResponseDTO>> getAllBatches() {
        return ResponseEntity.ok(batchService.getAllBatches());
    }

    @GetMapping("/{batchId}")
    @PreAuthorize(BATCH_READERS)
    public ResponseEntity<BatchResponseDTO> getBatchById(@PathVariable Long batchId) {
        return ResponseEntity.ok(batchService.getBatchById(batchId));
    }

    @GetMapping("/token/{tokenId}")
    @PreAuthorize(BATCH_READERS)
    public ResponseEntity<BatchResponseDTO> getBatchByTokenId(@PathVariable String tokenId) {
        return ResponseEntity.ok(batchService.getBatchByTokenId(tokenId));
    }
}
