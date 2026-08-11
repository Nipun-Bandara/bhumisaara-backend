package com.bandits.bhumisaara.controller;

import com.bandits.bhumisaara.dto.request.AssignFarmerAreaRequestDTO;
import com.bandits.bhumisaara.dto.response.FarmerAreaResponseDTO;
import com.bandits.bhumisaara.service.FarmerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/farmers")
@RequiredArgsConstructor
public class FarmerController {

    private final FarmerService farmerService;

    @GetMapping("/me/area")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<FarmerAreaResponseDTO> getMyArea() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(farmerService.getMyArea());
    }

    @PatchMapping("/me/area")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<FarmerAreaResponseDTO> updateMyArea(
            @Valid @RequestBody AssignFarmerAreaRequestDTO request) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(farmerService.updateMyArea(request));
    }
}
