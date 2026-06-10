package com.garden.server.controller;

import com.garden.server.dto.BedDto;
import com.garden.server.entity.User;
import com.garden.server.service.BedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/beds")
@RequiredArgsConstructor
public class BedController {

    private final BedService bedService;

    @GetMapping("/plot/{plotId}")
    public ResponseEntity<List<BedDto.Response>> getBedsByPlot(
            @PathVariable Long plotId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(bedService.getBedsByPlot(plotId, currentUser.getId()));
    }

    @PostMapping
    public ResponseEntity<BedDto.Response> createBed(
            @Valid @RequestBody BedDto.Request request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(bedService.createBed(request, currentUser.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BedDto.Response> updateBed(
            @PathVariable Long id,
            @Valid @RequestBody BedDto.Request request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(bedService.updateBed(id, request, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBed(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        bedService.deleteBed(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<BedDto.Response>> getUserBeds(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(bedService.getBedsByUser(currentUser.getId()));
    }
}