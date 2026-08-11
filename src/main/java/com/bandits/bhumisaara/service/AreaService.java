package com.bandits.bhumisaara.service;

import com.bandits.bhumisaara.dto.response.AreaResponseDTO;
import com.bandits.bhumisaara.entity.AreaEntity;
import com.bandits.bhumisaara.repository.AreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AreaService {

    private final AreaRepository areaRepository;

    @Transactional(readOnly = true)
    public List<AreaResponseDTO> getAllAreas() {
        return areaRepository.findAllByOrderByDistrictAscAreaNameAsc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AreaResponseDTO mapToDTO(AreaEntity entity) {
        return AreaResponseDTO.builder()
                .areaId(entity.getAreaId())
                .areaName(entity.getAreaName())
                .district(entity.getDistrict())
                .build();
    }
}
