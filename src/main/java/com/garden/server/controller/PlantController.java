package com.garden.server.controller;

import com.garden.server.dto.PlantDto;
import com.garden.server.entity.User;
import com.garden.server.service.PlantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plants")
@RequiredArgsConstructor
public class PlantController {

    private final PlantService plantService;

    @GetMapping
    public ResponseEntity<List<PlantDto.Response>> getUserPlants(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(plantService.getPlantsByUser(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantDto.Response> getPlantById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(plantService.getPlantById(id, currentUser.getId()));
    }

    @PostMapping
    public ResponseEntity<PlantDto.Response> createPlant(
            @Valid @RequestBody PlantDto.Request request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(plantService.createPlant(request, currentUser.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlantDto.Response> updatePlant(
            @PathVariable Long id,
            @Valid @RequestBody PlantDto.Request request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(plantService.updatePlant(id, request, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlant(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        plantService.deletePlant(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}