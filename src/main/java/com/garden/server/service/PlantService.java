package com.garden.server.service;

import com.garden.server.dto.PlantDto;
import com.garden.server.entity.Plant;
import com.garden.server.entity.User;
import com.garden.server.repository.PlantRepository;
import com.garden.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlantService {

    private final PlantRepository plantRepository;
    private final UserRepository userRepository;

    @Transactional
    public PlantDto.Response createPlant(PlantDto.Request request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Plant plant = Plant.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .categories(request.getCategories())
                .photosUri(request.getPhotosUri())
                .build();

        return mapToResponse(plantRepository.save(plant));
    }

    @Transactional(readOnly = true)
    public List<PlantDto.Response> getPlantsByUser(Long userId) {
        return plantRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlantDto.Response getPlantById(Long plantId, Long userId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new IllegalArgumentException("Растение не найдено"));

        if (!plant.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен: растение принадлежит другому пользователю");
        }

        return mapToResponse(plant);
    }

    @Transactional
    public PlantDto.Response updatePlant(Long plantId, PlantDto.Request request, Long userId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new IllegalArgumentException("Растение не найдено"));

        if (!plant.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен: вы не можете изменять чужое растение");
        }

        plant.setName(request.getName());
        plant.setDescription(request.getDescription());
        plant.setCategories(request.getCategories());
        plant.setPhotosUri(request.getPhotosUri());

        return mapToResponse(plantRepository.save(plant));
    }

    @Transactional
    public void deletePlant(Long plantId, Long userId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new IllegalArgumentException("Растение не найдено"));

        if (!plant.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен: вы не можете удалять чужое растение");
        }

        plantRepository.delete(plant);
    }

    private PlantDto.Response mapToResponse(Plant plant) {
        return PlantDto.Response.builder()
                .id(plant.getId())
                .name(plant.getName())
                .description(plant.getDescription())
                .categories(plant.getCategories())
                .photosUri(plant.getPhotosUri())
                .build();
    }

    @Transactional(readOnly = true)
    public boolean existsByIdAndUser(Long plantId, Long userId) {
        return plantRepository.findById(plantId)
                .map(plant -> plant.getUser().getId().equals(userId))
                .orElse(false);
    }
}