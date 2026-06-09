package com.garden.server.controller;

import com.garden.server.dto.TaskDto;
import com.garden.server.entity.User;
import com.garden.server.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskDto.Response>> getUserTasks(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.getTasksByUser(currentUser.getId()));
    }

    @PostMapping
    public ResponseEntity<TaskDto.Response> createTask(
            @Valid @RequestBody TaskDto.Request request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.createTask(request, currentUser.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDto.Response> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskDto.Request request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.updateTask(id, request, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        taskService.deleteTask(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}