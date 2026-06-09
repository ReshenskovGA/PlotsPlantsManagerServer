package com.garden.server.controller;

import com.garden.server.dto.TreebushDto;
import com.garden.server.entity.User;
import com.garden.server.service.TreebushService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/treebushes")
@RequiredArgsConstructor
public class TreebushController {

    private final TreebushService treebushService;

    @GetMapping("/plot/{plotId}")
    public ResponseEntity<List<TreebushDto.Response>> getTreebushesByPlot(
            @PathVariable Long plotId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(treebushService.getTreebushByPlot(plotId, currentUser.getId()));
    }

    @PostMapping
    public ResponseEntity<TreebushDto.Response> createTreebush(
            @Valid @RequestBody TreebushDto.Request request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(treebushService.createTreebush(request, currentUser.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TreebushDto.Response> updateTreebush(
            @PathVariable Long id,
            @Valid @RequestBody TreebushDto.Request request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(treebushService.updateTreebush(id, request, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTreebush(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        treebushService.deleteTreebush(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}