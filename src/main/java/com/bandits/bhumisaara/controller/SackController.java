package com.bandits.bhumisaara.controller;

import com.bandits.bhumisaara.dto.response.SackResponseDTO;
import com.bandits.bhumisaara.service.SackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sacks")
@RequiredArgsConstructor
public class SackController {

    private final SackService sackService;

    /** The calling officer's own stock — what is physically in their store. */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('AGRARIAN_SERVICE_OFFICER')")
    public ResponseEntity<List<SackResponseDTO>> getMySacks() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(sackService.getMySacks());
    }

    /** Label sheet source: every sack belonging to one batch, in label order. */
    @GetMapping("/batch/{batchId}")
    @PreAuthorize("hasRole('GOVERNMENT_ADMIN')")
    public ResponseEntity<List<SackResponseDTO>> getSacksForBatch(@PathVariable Long batchId) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(sackService.getSacksForBatch(batchId));
    }

    /**
     * Generates sacks for a batch minted before sacks existed. Idempotent — a
     * batch that already has sacks is returned as-is.
     */
    @PostMapping("/backfill/{batchId}")
    @PreAuthorize("hasRole('GOVERNMENT_ADMIN')")
    public ResponseEntity<List<SackResponseDTO>> backfillSacks(@PathVariable Long batchId) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(sackService.backfillSacksForBatch(batchId));
    }
}
