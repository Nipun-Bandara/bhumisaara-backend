package com.bandits.bhumisaara.controller;

import com.bandits.bhumisaara.dto.request.HandoverRequestDTO;
import com.bandits.bhumisaara.dto.response.DistributionResponseDTO;
import com.bandits.bhumisaara.service.DistributionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/distributions")
@RequiredArgsConstructor
public class DistributionController {

    private final DistributionService distributionService;

    @PostMapping
    public ResponseEntity<DistributionResponseDTO> recordHandover(@Valid @RequestBody HandoverRequestDTO request) {
        DistributionResponseDTO response = distributionService.recordHandover(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
