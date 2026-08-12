package com.bandits.bhumisaara.controller;

import com.bandits.bhumisaara.dto.request.HandoverRequestDTO;
import com.bandits.bhumisaara.dto.request.SackValidationRequestDTO;
import com.bandits.bhumisaara.dto.response.DistributionResponseDTO;
import com.bandits.bhumisaara.dto.response.PendingCollectionResponseDTO;
import com.bandits.bhumisaara.dto.response.SackValidationResponseDTO;
import com.bandits.bhumisaara.service.DistributionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/distributions")
@RequiredArgsConstructor
public class DistributionController {

    private final DistributionService distributionService;

    /** The officer's collection queue for their own area. */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('AGRARIAN_SERVICE_OFFICER')")
    public ResponseEntity<List<PendingCollectionResponseDTO>> getPendingCollections() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(distributionService.getPendingCollections());
    }

    /** Pre-flight check on a scanned sack; every rule is re-run on POST. */
    @PostMapping("/validate-sack")
    @PreAuthorize("hasRole('AGRARIAN_SERVICE_OFFICER')")
    public ResponseEntity<SackValidationResponseDTO> validateSack(
            @Valid @RequestBody SackValidationRequestDTO request) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(distributionService.validateSack(request));
    }

    @PostMapping
    @PreAuthorize("hasRole('AGRARIAN_SERVICE_OFFICER')")
    public ResponseEntity<DistributionResponseDTO> recordHandover(
            @Valid @RequestBody HandoverRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(distributionService.recordHandover(request));
    }

    @GetMapping("/officer")
    @PreAuthorize("hasRole('AGRARIAN_SERVICE_OFFICER')")
    public ResponseEntity<List<DistributionResponseDTO>> getOfficerDistributions() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(distributionService.getOfficerDistributions());
    }

    /** Every handover nationally — the ministry's burn ledger. */
    @GetMapping
    @PreAuthorize("hasAnyRole('GOVERNMENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<DistributionResponseDTO>> getAllDistributions() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(distributionService.getAllDistributions());
    }

    @GetMapping("/farmer")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<List<DistributionResponseDTO>> getFarmerDistributions() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(distributionService.getFarmerDistributions());
    }

    /** "I did not receive this" — flags the record, reverses nothing. */
    @PostMapping("/{distributionId}/dispute")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<DistributionResponseDTO> disputeDistribution(@PathVariable Long distributionId) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(distributionService.disputeDistribution(distributionId));
    }
}
