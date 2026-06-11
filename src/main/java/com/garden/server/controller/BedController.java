package com.garden.server.controller;

import com.garden.server.dto.BedDto;
import com.garden.server.entity.User;
import com.garden.server.security.CustomUserDetails;
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
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(bedService.getBedsByPlot(plotId, userDetails.getUser().getId()));
    }

    @PostMapping
    public ResponseEntity<BedDto.Response> createBed(
            @Valid @RequestBody BedDto.Request request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(bedService.createBed(request, userDetails.getUser().getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BedDto.Response> updateBed(
            @PathVariable Long id,
            @Valid @RequestBody BedDto.Request request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(bedService.updateBed(id, request, userDetails.getUser().getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBed(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        bedService.deleteBed(id, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<BedDto.Response>> getUserBeds(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(bedService.getBedsByUser(userDetails.getUser().getId()));
    }
}