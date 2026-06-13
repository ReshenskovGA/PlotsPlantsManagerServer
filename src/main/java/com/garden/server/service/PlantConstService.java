package com.garden.server.service;

import com.garden.server.dto.PlantDto;
import com.garden.server.entity.PlantConst;
import com.garden.server.repository.PlantConstRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlantConstService {

    private final PlantConstRepository plantConstRepository;

    @Transactional(readOnly = true)
    public List<PlantDto.ConstResponse> getAllPlants() {
        return plantConstRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlantDto.ConstResponse> searchPlants(String query) {
        return plantConstRepository.findByNameContainingIgnoreCase(query).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PlantDto.ConstResponse mapToResponse(PlantConst plant) {
        return PlantDto.ConstResponse.builder()
                .id(plant.getId())
                .name(plant.getName())
                .description(plant.getDescription())
                .categories(plant.getCategories())
                .photosUri(plant.getPhotosUri())
                .build();
    }
    @Transactional
    public PlantDto.ConstResponse createPlantConst(PlantDto.Request request) {
        PlantConst plant = PlantConst.builder()
                .name(request.getName())
                .description(request.getDescription())
                .categories(request.getCategories())
                .photosUri(request.getPhotosUri())
                .build();
        return mapToResponse(plantConstRepository.save(plant));
    }

    @Transactional
    public PlantDto.ConstResponse updatePlantConst(Long id, PlantDto.Request request) {
        PlantConst plant = plantConstRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Растение справочника не найдено"));
        plant.setName(request.getName());
        plant.setDescription(request.getDescription());
        plant.setCategories(request.getCategories());
        plant.setPhotosUri(request.getPhotosUri());
        return mapToResponse(plantConstRepository.save(plant));
    }

    @Transactional
    public void deletePlantConst(Long id) {
        plantConstRepository.deleteById(id);
    }
}