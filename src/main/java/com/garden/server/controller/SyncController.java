package com.garden.server.controller;

import com.garden.server.dto.SyncDto;
import com.garden.server.entity.User;
import com.garden.server.service.SyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/process")
    public ResponseEntity<SyncDto.SyncResponse> processSync(
            @Valid @RequestBody SyncDto.SyncRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SyncDto.SyncResponse response = syncService.processSync(request, userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }
}