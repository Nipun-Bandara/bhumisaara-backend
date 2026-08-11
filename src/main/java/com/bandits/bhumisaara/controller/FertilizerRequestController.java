package com.bandits.bhumisaara.controller;

import com.bandits.bhumisaara.dto.request.FertilizerRequestCreateDTO;
import com.bandits.bhumisaara.dto.request.FertilizerRequestReviewDTO;
import com.bandits.bhumisaara.dto.response.FertilizerRequestResponseDTO;
import com.bandits.bhumisaara.enums.RequestStatus;
import com.bandits.bhumisaara.service.FertilizerRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fertilizer-requests")
@RequiredArgsConstructor
public class FertilizerRequestController {

    private final FertilizerRequestService requestService;

    @PostMapping
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<FertilizerRequestResponseDTO> createRequest(
            @Valid @RequestBody FertilizerRequestCreateDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestService.createRequest(request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<List<FertilizerRequestResponseDTO>> getMyRequests() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestService.getMyRequests());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('AGRARIAN_SERVICE_OFFICER')")
    public ResponseEntity<List<FertilizerRequestResponseDTO>> getPendingRequests() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestService.getPendingRequestsForOfficer());
    }

    @GetMapping("/area")
    @PreAuthorize("hasRole('AGRARIAN_SERVICE_OFFICER')")
    public ResponseEntity<List<FertilizerRequestResponseDTO>> getAreaRequests(
            @RequestParam(value = "status", required = false) RequestStatus status) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestService.getAreaRequestsForOfficer(status));
    }

    @PatchMapping("/{requestId}/review")
    @PreAuthorize("hasRole('AGRARIAN_SERVICE_OFFICER')")
    public ResponseEntity<FertilizerRequestResponseDTO> reviewRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody FertilizerRequestReviewDTO review) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestService.reviewRequest(requestId, review));
    }
}
