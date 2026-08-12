package com.bandits.bhumisaara.controller;

import com.bandits.bhumisaara.dto.response.AreaResponseDTO;
import com.bandits.bhumisaara.service.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/areas")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;

    /**
     * Reference data, readable by any signed-in user: government admins pick an
     * area when assigning officers, farmers pick their own on their profile.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AreaResponseDTO>> getAllAreas() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(areaService.getAllAreas());
    }
}
