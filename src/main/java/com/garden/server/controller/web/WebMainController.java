package com.garden.server.controller.web;

import com.garden.server.dto.BedDto;
import com.garden.server.dto.PlotDto;
import com.garden.server.dto.TaskDto;
import com.garden.server.dto.TreebushDto;
import com.garden.server.entity.Plant;
import com.garden.server.entity.PlantConst;
import com.garden.server.entity.User;
import com.garden.server.repository.PlantConstRepository;
import com.garden.server.repository.PlantRepository;
import com.garden.server.repository.UserRepository;
import com.garden.server.service.*;
import com.garden.server.utils.RecurrenceUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/web")
@RequiredArgsConstructor
public class WebMainController {
    private final PlotService plotService;
    private final BedService bedService;
    private final TreebushService treebushService;
    private final TaskService taskService;
    private final PlantConstService plantConstService;
    private final UserRepository userRepository;
    private final PlantRepository plantRepository;
    private final PlantConstRepository plantConstRepository;
    private final RecurrenceUtils recurrenceUtils;

    private Long getCurrentUserId(Authentication authentication) {
        String login = authentication.getName();
        return userRepository.findByLogin(login)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден в сессии"));
    }

    private String getPlantName(Long plantId) {
        if (plantId == null) return "Не посажено";
        if (plantId < 0) {
            return plantConstRepository.findById(Math.abs(plantId)).map(PlantConst::getName).orElse("Растение");
        } else {
            return plantRepository.findById(plantId).map(Plant::getName).orElse("Растение");
        }
    }

    private LocalDate toLocalDate(Long timestamp) {
        if (timestamp == null) return null;
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Map<String, String> getPlantedItemNames(Long userId) {
        Map<String, String> names = new HashMap<>();
        List<PlotDto.Response> plots = plotService.getPlotsByUser(userId);
        for (PlotDto.Response plot : plots) {
            List<BedDto.Response> beds = bedService.getBedsByPlot(plot.getId(), userId);
            for (BedDto.Response bed : beds) {
                String plantName = getPlantName(bed.getPlantId());
                names.put("BED_" + bed.getId(), plot.getName() + " - Грядка (" + plantName + ")");
            }
            List<TreebushDto.Response> trees = treebushService.getTreebushByPlot(plot.getId(), userId);
            for (TreebushDto.Response tree : trees) {
                String plantName = getPlantName(tree.getPlantId());
                names.put("TREEBUSH_" + tree.getId(), plot.getName() + " - Дерево/Куст (" + plantName + ")");
            }
        }
        return names;
    }

    @Data
    @AllArgsConstructor
    public static class DashboardTask {
        private Long id;
        private String title;
        private String formattedDate;
        private Long date;
        private String categoryName;
        private Boolean isCompleted;
        private String status;
        private Long plantedItemId;
        private String plantedItemType;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Long userId = getCurrentUserId(auth);
        User user = userRepository.findById(userId).get();
        model.addAttribute("user", user);

        List<PlotDto.Response> plots = plotService.getPlotsByUser(userId);
        model.addAttribute("plotsCount", plots.size());

        List<TaskDto.Response> allTasks = taskService.getTasksByUser(userId);

        int gardenItemsCount = 0;
        Set<Long> plantedPlantIds = new HashSet<>();
        for (PlotDto.Response plot : plots) {
            List<BedDto.Response> beds = bedService.getBedsByPlot(plot.getId(), userId);
            gardenItemsCount += beds.size();
            for (BedDto.Response bed : beds) {
                if (bed.getPlantId() != null) {
                    plantedPlantIds.add(bed.getPlantId());
                }
            }
            List<TreebushDto.Response> trees = treebushService.getTreebushByPlot(plot.getId(), userId);
            gardenItemsCount += trees.size();
            for (TreebushDto.Response tree : trees) {
                if (tree.getPlantId() != null) {
                    plantedPlantIds.add(tree.getPlantId());
                }
            }
        }
        model.addAttribute("gardenItemsCount", gardenItemsCount);
        model.addAttribute("plantedPlantsCount", plantedPlantIds.size());

        LocalDate now = LocalDate.now();
        List<TaskDto.Response> expandedTasks = recurrenceUtils.expandTasks(
                allTasks, now.minusMonths(1), now.plusMonths(1));
        model.addAttribute("tasksCount", expandedTasks.size());

        long calendarTasksCount = expandedTasks.stream()
                .filter(t -> !t.getIsCompleted())
                .count();
        model.addAttribute("calendarTasksCount", calendarTasksCount);

        long startOfToday = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endOfToday = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1;

        List<DashboardTask> todayTasks = new ArrayList<>();
        for (TaskDto.Response task : expandedTasks) {
            if (task.getDate() == null) continue;

            String status;
            if (task.getDate() < startOfToday) {
                if (task.getIsCompleted()) continue;
                status = "overdue";
            } else if (task.getDate() <= endOfToday) {
                status = task.getIsCompleted() ? "completed" : "today";
            } else {
                continue;
            }

            todayTasks.add(new DashboardTask(
                    task.getId(),
                    task.getTitle(),
                    toLocalDate(task.getDate()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    task.getDate(),
                    task.getCategoryName(),
                    task.getIsCompleted(),
                    status,
                    task.getPlantedItemId(),
                    task.getPlantedItemType()
            ));
        }

        todayTasks.sort(Comparator.comparing((DashboardTask t) -> {
            switch (t.getStatus()) {
                case "overdue": return 0;
                case "today": return 1;
                case "completed": return 2;
                default: return 3;
            }
        }).thenComparing(DashboardTask::getDate));

        model.addAttribute("todayTasks", todayTasks);
        model.addAttribute("plantedItemNames", getPlantedItemNames(userId));
        return "dashboard";
    }

    @GetMapping("/items")
    public String gardenItems(Authentication auth, Model model) {
        Long userId = getCurrentUserId(auth);
        List<PlotDto.Response> plots = plotService.getPlotsByUser(userId);
        List<GardenItemViewModel> items = new ArrayList<>();
        for (PlotDto.Response plot : plots) {
            List<BedDto.Response> beds = bedService.getBedsByPlot(plot.getId(), userId);
            for (BedDto.Response bed : beds) {
                items.add(new GardenItemViewModel(
                        bed.getId(), "Грядка", plot.getName(),
                        getPlantName(bed.getPlantId()),
                        bed.getLength() + " x " + bed.getWidth() + " м, Смещение: (" + bed.getOffsetX() + ", " + bed.getOffsetY() + ")",
                        bed.getMarkerColor()));
            }
            List<TreebushDto.Response> trees = treebushService.getTreebushByPlot(plot.getId(), userId);
            for (TreebushDto.Response tree : trees) {
                items.add(new GardenItemViewModel(
                        tree.getId(), "Дерево/Куст", plot.getName(),
                        getPlantName(tree.getPlantId()),
                        "Диаметр: " + tree.getDiameter() + " м, Смещение: (" + tree.getOffsetX() + ", " + tree.getOffsetY() + ")",
                        tree.getMarkerColor()));
            }
        }
        model.addAttribute("items", items);
        return "garden-items";
    }

    public static class GardenItemViewModel {
        public Long id;
        public String type;
        public String plotName;
        public String plantName;
        public String params;
        public String colorHex;

        public GardenItemViewModel(Long id, String type, String plotName, String plantName, String params, Integer markerColor) {
            this.id = id;
            this.type = type;
            this.plotName = plotName;
            this.plantName = plantName;
            this.params = params;
            this.colorHex = intToHexColor(markerColor);
        }

        private static String intToHexColor(Integer color) {
            if (color == null) return "#9E9E9E";
            return String.format("#%06X", (0xFFFFFF & color));
        }
    }
}