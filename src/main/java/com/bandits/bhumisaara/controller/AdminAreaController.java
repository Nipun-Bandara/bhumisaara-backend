package com.bandits.bhumisaara.controller;

import com.bandits.bhumisaara.dto.request.AreaRequestDTO;
import com.bandits.bhumisaara.dto.response.AdminAreaResponseDTO;
import com.bandits.bhumisaara.dto.response.AreaCoverageResponseDTO;
import com.bandits.bhumisaara.service.AdminAreaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Area administration.
 * <p>
 * Note the absence of a {@code DELETE}: areas are deactivated, never removed,
 * because farmers, requests, transfers and handovers all point at one.
 */
@RestController
@RequestMapping("/admin/areas")
@RequiredArgsConstructor
public class AdminAreaController {

    private final AdminAreaService adminAreaService;

    /** Every area with its serving officer, or the vacancy and what it costs. */
    @GetMapping("/coverage")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<AreaCoverageResponseDTO>> getCoverage() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(adminAreaService.getCoverage());
    }

    /** Every area including deactivated ones — {@code GET /areas} hides those. */
    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<AdminAreaResponseDTO>> getAreas() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(adminAreaService.getAreas());
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<AdminAreaResponseDTO> createArea(@Valid @RequestBody AreaRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(adminAreaService.createArea(request));
    }

    @PatchMapping("/{areaId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<AdminAreaResponseDTO> updateArea(
            @PathVariable Long areaId,
            @Valid @RequestBody AreaRequestDTO request) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(adminAreaService.updateArea(areaId, request));
    }

    /** The soft delete. Refused while an officer or any farmer still points here. */
    @PostMapping("/{areaId}/deactivate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<AdminAreaResponseDTO> deactivateArea(@PathVariable Long areaId) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(adminAreaService.deactivateArea(areaId));
    }

    @PostMapping("/{areaId}/activate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<AdminAreaResponseDTO> activateArea(@PathVariable Long areaId) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(adminAreaService.activateArea(areaId));
    }
}
