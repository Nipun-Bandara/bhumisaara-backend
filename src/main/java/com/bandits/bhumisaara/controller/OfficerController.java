package com.bandits.bhumisaara.controller;

import com.bandits.bhumisaara.dto.request.AssignOfficerRequestDTO;
import com.bandits.bhumisaara.dto.response.OfficerResponseDTO;
import com.bandits.bhumisaara.service.OfficerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/officers")
@RequiredArgsConstructor
public class OfficerController {

    private final OfficerService officerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('GOVERNMENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<OfficerResponseDTO>> getOfficers(
            @RequestParam(value = "assigned", required = false) Boolean assigned) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(officerService.getOfficers(assigned));
    }

    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('GOVERNMENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<OfficerResponseDTO> assignOfficerToArea(
            @Valid @RequestBody AssignOfficerRequestDTO request) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(officerService.assignOfficerToArea(request));
    }
}
