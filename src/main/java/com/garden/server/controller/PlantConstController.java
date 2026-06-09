package com.garden.server.controller;

import com.garden.server.dto.PlantDto;
import com.garden.server.service.PlantConstService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plants-const")
@RequiredArgsConstructor
public class PlantConstController {

    private final PlantConstService plantConstService;

    @GetMapping
    public ResponseEntity<List<PlantDto.ConstResponse>> getAllPlants() {
        return ResponseEntity.ok(plantConstService.getAllPlants());
    }

    @GetMapping("/search")
    public ResponseEntity<List<PlantDto.ConstResponse>> searchPlants(@RequestParam String query) {
        return ResponseEntity.ok(plantConstService.searchPlants(query));
    }
}