package com.bandits.bhumisaara.controller;

import com.bandits.bhumisaara.dto.request.IssueCreditsRequestDTO;
import com.bandits.bhumisaara.dto.response.CreditBalanceResponseDTO;
import com.bandits.bhumisaara.dto.response.CreditIssuanceResponseDTO;
import com.bandits.bhumisaara.dto.response.CreditReconciliationResponseDTO;
import com.bandits.bhumisaara.dto.response.EligibleFarmerResponseDTO;
import com.bandits.bhumisaara.service.CreditOversightService;
import com.bandits.bhumisaara.service.SubsidyCreditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Subsidy credits: the treasury minting a season's budget, and the farmer's own
 * view of what they hold.
 * <p>
 * Nothing here touches stock tokens — a credit is a claim on the treasury, not
 * on a warehouse.
 */
@RestController
@RequestMapping("/credits")
@RequiredArgsConstructor
public class SubsidyCreditController {

    private final SubsidyCreditService subsidyCreditService;
    private final CreditOversightService creditOversightService;

    /** Seasons an admin may issue against — filed requests plus funded seasons. */
    @GetMapping("/seasons")
    @PreAuthorize("hasAnyRole('GOVERNMENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<String>> getSeasons() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(subsidyCreditService.getSeasons());
    }

    /** Every farmer, annotated with whether this season already reached them. */
    @GetMapping("/eligible-farmers")
    @PreAuthorize("hasRole('GOVERNMENT_ADMIN')")
    public ResponseEntity<List<EligibleFarmerResponseDTO>> getEligibleFarmers(
            @RequestParam String season) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(subsidyCreditService.getEligibleFarmers(season));
    }

    /** Records a credit mint that already confirmed on-chain. */
    @PostMapping("/issue")
    @PreAuthorize("hasRole('GOVERNMENT_ADMIN')")
    public ResponseEntity<CreditIssuanceResponseDTO> issueCredits(
            @Valid @RequestBody IssueCreditsRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(subsidyCreditService.issueCredits(request));
    }

    /** Every issuance nationally — the treasury's mint ledger. */
    @GetMapping("/issuances")
    @PreAuthorize("hasAnyRole('GOVERNMENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<CreditIssuanceResponseDTO>> getAllIssuances() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(subsidyCreditService.getAllIssuances());
    }

    /**
     * The calling farmer's own credit position: the Postgres ledger plus the
     * season token ids to read {@code balanceOf} against in the browser.
     */
    @GetMapping("/balance")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<CreditBalanceResponseDTO> getMyBalance() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(subsidyCreditService.getMyBalance());
    }

    /** Issued vs redeemed vs still held. A non-zero discrepancy is an anomaly. */
    @GetMapping("/reconciliation")
    @PreAuthorize("hasAnyRole('GOVERNMENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<CreditReconciliationResponseDTO> getReconciliation(
            @RequestParam(required = false) String season) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(creditOversightService.getReconciliation(season));
    }
}
