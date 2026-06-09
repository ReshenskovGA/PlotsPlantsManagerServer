package com.garden.server.controller.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garden.server.dto.TaskDto;
import com.garden.server.entity.User;
import com.garden.server.model.RecurrenceRule;
import com.garden.server.repository.UserRepository;
import com.garden.server.service.TaskService;
import com.garden.server.utils.RecurrenceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/web/tasks")
@RequiredArgsConstructor
public class WebTaskController {
    private final TaskService taskService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final RecurrenceUtils recurrenceUtils;

    private Long getCurrentUserId(Authentication authentication) {
        String login = authentication.getName();
        return userRepository.findByLogin(login)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден в сессии"));
    }

    private LocalDate toLocalDate(Long timestamp) {
        if (timestamp == null) return null;
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private long toTimestamp(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private TaskDto.Request toRequest(TaskDto.Response r) {
        TaskDto.Request req = new TaskDto.Request();
        req.setId(r.getId());
        req.setTitle(r.getTitle());
        req.setDescription(r.getDescription());
        req.setDate(r.getDate());
        req.setIsCompleted(r.getIsCompleted());
        req.setCategoryName(r.getCategoryName());
        req.setRecurrenceRuleJson(r.getRecurrenceRuleJson());
        req.setPlotId(r.getPlotId());
        req.setPlantedItemId(r.getPlantedItemId());
        req.setPlantedItemType(r.getPlantedItemType());
        return req;
    }

    @GetMapping
    public String listTasks(Authentication auth,
                            @RequestParam(required = false, defaultValue = "all") String filter, Model model) {
        Long userId = getCurrentUserId(auth);
        List<TaskDto.Response> tasks = taskService.getTasksByUser(userId);

        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusMonths(1);
        LocalDate endDate = now.plusYears(1);
        List<TaskDto.Response> expandedTasks = recurrenceUtils.expandTasks(tasks, startDate, endDate);

        if ("pending".equals(filter)) {
            expandedTasks = expandedTasks.stream().filter(t -> !t.getIsCompleted()).toList();
        } else if ("completed".equals(filter)) {
            expandedTasks = expandedTasks.stream().filter(TaskDto.Response::getIsCompleted).toList();
        }

        model.addAttribute("tasks", expandedTasks);
        model.addAttribute("currentFilter", filter);
        return "tasks";
    }

    @GetMapping("/calendar")
    public String calendarView(Authentication auth, Model model) {
        Long userId = getCurrentUserId(auth);
        List<TaskDto.Response> tasks = taskService.getTasksByUser(userId);

        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusYears(1);
        LocalDate endDate = now.plusYears(1);
        List<TaskDto.Response> expandedTasks = recurrenceUtils.expandTasks(tasks, startDate, endDate);

        model.addAttribute("tasks", expandedTasks);
        return "calendar";
    }

    @GetMapping("/day")
    public String listTasksByDay(@RequestParam String date, Authentication auth, Model model) {
        Long userId = getCurrentUserId(auth);
        LocalDate targetDate = LocalDate.parse(date);
        List<TaskDto.Response> tasks = taskService.getTasksByUser(userId);
        List<TaskDto.Response> expandedTasks = recurrenceUtils.expandTasks(tasks, targetDate, targetDate);

        model.addAttribute("tasks", expandedTasks);
        model.addAttribute("selectedDate", date);
        model.addAttribute("formattedDate", targetDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        return "tasks-day";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        TaskDto.Request task = new TaskDto.Request();
        task.setIsCompleted(false);
        model.addAttribute("task", task);
        model.addAttribute("dateString", LocalDate.now().toString());
        return "task-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Authentication auth, Model model) {
        Long userId = getCurrentUserId(auth);
        TaskDto.Response task = taskService.getTaskById(id, userId);
        model.addAttribute("task", toRequest(task));
        String dateString = "";
        if (task.getDate() != null) {
            dateString = toLocalDate(task.getDate()).toString();
        }
        model.addAttribute("dateString", dateString);
        return "task-form";
    }

    @GetMapping("/view/{id}")
    public String viewTask(@PathVariable Long id, Authentication auth, Model model) {
        Long userId = getCurrentUserId(auth);
        long baseId = Math.abs(id);
        if (id < 0) {
            baseId = Math.abs(id) / 10000;
        }
        TaskDto.Response task = taskService.getTaskById(baseId, userId);
        model.addAttribute("task", task);
        model.addAttribute("isOccurrence", id < 0);

        if (task.getDate() != null) {
            model.addAttribute("formattedDate", toLocalDate(task.getDate()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        } else {
            model.addAttribute("formattedDate", "Не указана");
        }

        if (task.getRecurrenceRuleJson() != null) {
            model.addAttribute("recurrenceSummary", getRecurrenceSummary(task.getRecurrenceRuleJson()));
        } else {
            model.addAttribute("recurrenceSummary", "Не повторяется");
        }

        return "task-view";
    }

    @PostMapping("/save")
    public String saveTask(@ModelAttribute TaskDto.Request request,
                           @RequestParam("dateString") String dateString,
                           Authentication auth) {
        Long userId = getCurrentUserId(auth);
        if (dateString != null && !dateString.isEmpty()) {
            request.setDate(toTimestamp(LocalDate.parse(dateString)));
        }
        if (request.getId() != null) {
            taskService.updateTask(request.getId(), request, userId);
        } else {
            taskService.createTask(request, userId);
        }
        return "redirect:/web/tasks";
    }

    @PostMapping("/toggle")
    public String toggleComplete(@RequestParam Long id, @RequestParam Long date, Authentication auth) {
        Long userId = getCurrentUserId(auth);
        long baseId = Math.abs(id);
        if (id < 0) {
            baseId = Math.abs(id) / 10000;
        }

        TaskDto.Response task = taskService.getTaskById(baseId, userId);
        TaskDto.Request updateReq = toRequest(task);

        if (id < 0) {
            try {
                RecurrenceRule rule = objectMapper.readValue(task.getRecurrenceRuleJson(), RecurrenceRule.class);
                List<Long> completedDates = rule.getCompletedInstanceDates() != null ? new ArrayList<>(rule.getCompletedInstanceDates()) : new ArrayList<>();

                boolean isCurrentlyCompleted = completedDates.contains(date);
                if (!isCurrentlyCompleted) {
                    completedDates.add(date);
                } else {
                    completedDates.remove(date);
                }

                rule.setCompletedInstanceDates(completedDates.isEmpty() ? null : completedDates);
                updateReq.setRecurrenceRuleJson(objectMapper.writeValueAsString(rule));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            updateReq.setIsCompleted(!task.getIsCompleted());
        }

        taskService.updateTask(baseId, updateReq, userId);
        return "redirect:/web/tasks";
    }

    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id, Authentication auth) {
        Long userId = getCurrentUserId(auth);
        long baseId = Math.abs(id);
        if (id < 0) {
            baseId = Math.abs(id) / 10000;
        }
        taskService.deleteTask(baseId, userId);
        return "redirect:/web/tasks";
    }

    private String getRecurrenceSummary(String recurrenceRuleJson) {
        if (recurrenceRuleJson == null || recurrenceRuleJson.trim().isEmpty()) {
            return "Не повторяется";
        }
        try {
            JsonNode rule = objectMapper.readTree(recurrenceRuleJson);
            String type = rule.has("type") ? rule.get("type").asText() : "NONE";
            int interval = rule.has("interval") ? rule.get("interval").asInt() : 1;

            switch (type) {
                case "DAILY": return interval == 1 ? "Каждый день" : "Каждые " + interval + " дн.";
                case "WEEKLY": return interval == 1 ? "Каждую неделю" : "Каждые " + interval + " нед.";
                case "MONTHLY": return interval == 1 ? "Каждый месяц" : "Каждые " + interval + " мес.";
                case "YEARLY": return interval == 1 ? "Каждый год" : "Каждые " + interval + " г.";
                case "CUSTOM": return "Настраиваемое повторение";
                default: return "Не повторяется";
            }
        } catch (Exception e) {
            return "Не повторяется";
        }
    }
}