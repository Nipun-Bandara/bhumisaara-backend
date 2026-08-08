package com.bandits.bhumisaara.controller;

import com.bandits.bhumisaara.dto.request.BatchRequestDTO;
import com.bandits.bhumisaara.dto.response.BatchResponseDTO;
import com.bandits.bhumisaara.service.FertilizerBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
public class FertilizerBatchController {

    private final FertilizerBatchService batchService;

    @PostMapping
    public ResponseEntity<BatchResponseDTO> createBatch(@Valid @RequestBody BatchRequestDTO request) {
        BatchResponseDTO response = batchService.createBatch(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<BatchResponseDTO>> getAllBatches() {
        return ResponseEntity.ok(batchService.getAllBatches());
    }

    @GetMapping("/{batchId}")
    public ResponseEntity<BatchResponseDTO> getBatchById(@PathVariable Long batchId) {
        return ResponseEntity.ok(batchService.getBatchById(batchId));
    }

    @GetMapping("/token/{tokenId}")
    public ResponseEntity<BatchResponseDTO> getBatchByTokenId(@PathVariable String tokenId) {
        return ResponseEntity.ok(batchService.getBatchByTokenId(tokenId));
    }
}
