package com.garden.server.controller;

import com.garden.server.dto.PlotDto;
import com.garden.server.entity.User;
import com.garden.server.security.CustomUserDetails;
import com.garden.server.service.PlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plots")
@RequiredArgsConstructor
public class PlotController {

    private final PlotService plotService;

    @GetMapping
    public ResponseEntity<List<PlotDto.Response>> getUserPlots(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(plotService.getPlotsByUser(userDetails.getUser().getId()));
    }

    @PostMapping
    public ResponseEntity<PlotDto.Response> createPlot(
            @Valid @RequestBody PlotDto.Request request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(plotService.createPlot(request, userDetails.getUser().getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlotDto.Response> updatePlot(
            @PathVariable Long id,
            @Valid @RequestBody PlotDto.Request request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(plotService.updatePlot(id, request, userDetails.getUser().getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlot(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        plotService.deletePlot(id, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}