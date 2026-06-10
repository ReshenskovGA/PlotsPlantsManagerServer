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
    public ResponseEntity<List<PlantDto.Response>> getUserPlants(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(plantService.getPlantsByUser(userDetails.getUser().getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantDto.Response> getPlantById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(plantService.getPlantById(id, userDetails.getUser().getId()));
    }

    @PostMapping
    public ResponseEntity<PlantDto.Response> createPlant(
            @Valid @RequestBody PlantDto.Request request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(plantService.createPlant(request, userDetails.getUser().getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlantDto.Response> updatePlant(
            @PathVariable Long id,
            @Valid @RequestBody PlantDto.Request request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(plantService.updatePlant(id, request, userDetails.getUser().getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlant(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        plantService.deletePlant(id, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}