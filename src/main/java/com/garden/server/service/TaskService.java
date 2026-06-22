package com.garden.server.service;

import com.garden.server.dto.TaskDto;
import com.garden.server.entity.Plot;
import com.garden.server.entity.Task;
import com.garden.server.entity.User;
import com.garden.server.repository.PlotRepository;
import com.garden.server.repository.TaskRepository;
import com.garden.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PlotRepository plotRepository;

    @Transactional
    public TaskDto.Response createTask(TaskDto.Request request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        Plot plot = null;
        if (request.getPlotId() != null) {
            plot = plotRepository.findById(request.getPlotId())
                    .orElseThrow(() -> new IllegalArgumentException("Участок не найден"));
            if (!plot.getUser().getId().equals(userId)) {
                throw new SecurityException("Доступ запрещен к участку задачи");
            }
        }
        Task task = Task.builder()
                .user(user)
                .plot(plot)
                .title(request.getTitle())
                .description(request.getDescription())
                .date(request.getDate())
                .isCompleted(request.getIsCompleted())
                .recurrenceRuleJson(request.getRecurrenceRuleJson())
                .categoryName(request.getCategoryName())
                .plantedItemId(request.getPlantedItemId())
                .plantedItemType(request.getPlantedItemType())
                .build();
        return mapToResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskDto.Response> getTasksByUser(Long userId) {
        return taskRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskDto.Response getTaskById(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Задача не найдена"));
        if (!task.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен: задача принадлежит другому пользователю");
        }
        return mapToResponse(task);
    }

    @Transactional
    public TaskDto.Response updateTask(Long taskId, TaskDto.Request request, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Задача не найдена"));
        if (!task.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен");
        }

        if (request.getPlotId() != null) {
            if (task.getPlot() == null || !task.getPlot().getId().equals(request.getPlotId())) {
                Plot newPlot = plotRepository.findById(request.getPlotId())
                        .orElseThrow(() -> new IllegalArgumentException("Участок не найден"));
                if (!newPlot.getUser().getId().equals(userId)) {
                    throw new SecurityException("Доступ запрещен к новому участку");
                }
                task.setPlot(newPlot);
            }
        } else {
            task.setPlot(null);
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDate(request.getDate());
        task.setIsCompleted(request.getIsCompleted());
        task.setRecurrenceRuleJson(request.getRecurrenceRuleJson());
        task.setCategoryName(request.getCategoryName());
        task.setPlantedItemId(request.getPlantedItemId());
        task.setPlantedItemType(request.getPlantedItemType());

        return mapToResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Задача не найдена"));
        if (!task.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен");
        }
        taskRepository.delete(task);
    }

    private TaskDto.Response mapToResponse(Task task) {
        return TaskDto.Response.builder()
                .id(task.getId())
                .userId(task.getUser().getId())
                .plotId(task.getPlot() != null ? task.getPlot().getId() : null)
                .title(task.getTitle())
                .description(task.getDescription())
                .date(task.getDate())
                .isCompleted(task.getIsCompleted())
                .recurrenceRuleJson(task.getRecurrenceRuleJson())
                .categoryName(task.getCategoryName())
                .plantedItemId(task.getPlantedItemId())
                .plantedItemType(task.getPlantedItemType())
                .build();
    }
    @Transactional(readOnly = true)
    public boolean existsByIdAndUser(Long taskId, Long userId) {
        return taskRepository.findById(taskId)
                .map(task -> task.getUser().getId().equals(userId))
                .orElse(false);
    }
}