package com.garden.server.controller.web;

import com.garden.server.dto.BedDto;
import com.garden.server.dto.PlotDto;
import com.garden.server.dto.TreebushDto;
import com.garden.server.entity.Plant;
import com.garden.server.entity.PlantConst;
import com.garden.server.entity.User;
import com.garden.server.repository.PlantConstRepository;
import com.garden.server.repository.PlantRepository;
import com.garden.server.repository.UserRepository;
import com.garden.server.service.BedService;
import com.garden.server.service.PlotService;
import com.garden.server.service.TreebushService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
@RequestMapping("/web/plantedplants")
@RequiredArgsConstructor
public class WebPlantedPlantsController {
    private final PlotService plotService;
    private final BedService bedService;
    private final TreebushService treebushService;
    private final PlantRepository plantRepository;
    private final PlantConstRepository plantConstRepository;
    private final UserRepository userRepository;

    private Long getCurrentUserId(Authentication authentication) {
        String login = authentication.getName();
        return userRepository.findByLogin(login)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден в сессии"));
    }

    // Класс для хранения информации о местоположении растения
    @Data
    @AllArgsConstructor
    public static class PlantLocation {
        private Long plotId;
        private String plotName;
        private String objectType; // "Грядка" или "Дерево/Куст"
        private Long objectId;     // ID грядки или дерева
    }

    // Класс для детальной информации о посаженном растении
    @Data
    @AllArgsConstructor
    public static class PlantedPlantDetails {
        private Long id;
        private String name;
        private String description;
        private List<String> categories;
        private List<String> photosUri;
        private Boolean catalog; // Изменено на Boolean для корректной работы геттера getCatalog() в Thymeleaf
        private List<PlantLocation> locations = new ArrayList<>();
    }

    @GetMapping
    public String listPlantedPlants(Authentication auth, Model model) {
        Long userId = getCurrentUserId(auth);
        List<PlotDto.Response> plots = plotService.getPlotsByUser(userId);

        // Используем Map для группировки растений и их мест посадки
        Map<Long, PlantedPlantDetails> plantMap = new LinkedHashMap<>();

        for (PlotDto.Response plot : plots) {
            // Обработка грядок
            List<BedDto.Response> beds = bedService.getBedsByPlot(plot.getId(), userId);
            for (BedDto.Response bed : beds) {
                if (bed.getPlantId() != null) {
                    addPlantToMap(plantMap, bed.getPlantId(), plot, "Грядка", bed.getId());
                }
            }
            // Обработка деревьев/кустов
            List<TreebushDto.Response> trees = treebushService.getTreebushByPlot(plot.getId(), userId);
            for (TreebushDto.Response tree : trees) {
                if (tree.getPlantId() != null) {
                    addPlantToMap(plantMap, tree.getPlantId(), plot, "Дерево/Куст", tree.getId());
                }
            }
        }

        // Преобразуем Map в список и сортируем по названию
        List<PlantedPlantDetails> items = new ArrayList<>(plantMap.values());
        items.sort(Comparator.comparing(PlantedPlantDetails::getName));

        model.addAttribute("plants", items);
        return "planted-plants";
    }

    private void addPlantToMap(Map<Long, PlantedPlantDetails> map, Long plantId, PlotDto.Response plot, String objectType, Long objectId) {
        boolean isCatalog = plantId < 0;
        Long absId = Math.abs(plantId);

        PlantedPlantDetails details = map.computeIfAbsent(plantId, id -> {
            if (isCatalog) {
                PlantConst p = plantConstRepository.findById(absId).orElse(null);
                if (p == null) return null;
                return new PlantedPlantDetails(absId, p.getName(), p.getDescription(), p.getCategories(), p.getPhotosUri(), true, new ArrayList<>());
            } else {
                Plant p = plantRepository.findById(absId).orElse(null);
                if (p == null) return null;
                return new PlantedPlantDetails(absId, p.getName(), p.getDescription(), p.getCategories(), p.getPhotosUri(), false, new ArrayList<>());
            }
        });

        if (details != null) {
            details.getLocations().add(new PlantLocation(plot.getId(), plot.getName(), objectType, objectId));
        }
    }

    // Просмотр детальной информации о посаженном растении

    @GetMapping("/view/{plantId}")
    public String viewPlantedPlant(@PathVariable Long plantId,
                                   @RequestParam String source,
                                   Authentication auth,
                                   Model model) {
        Long userId = getCurrentUserId(auth);

        // Определяем тип растения по параметру source и формируем правильный ID со знаком
        boolean isCatalog = "catalog".equals(source);
        Long absId = Math.abs(plantId);
        Long signedPlantId = isCatalog ? -absId : absId;

        PlantedPlantDetails details;
        if (isCatalog) {
            PlantConst p = plantConstRepository.findById(absId)
                    .orElseThrow(() -> new IllegalArgumentException("Растение не найдено"));
            details = new PlantedPlantDetails(signedPlantId, p.getName(), p.getDescription(), p.getCategories(), p.getPhotosUri(), true, new ArrayList<>());
        } else {
            Plant p = plantRepository.findById(absId)
                    .orElseThrow(() -> new IllegalArgumentException("Растение не найдено"));
            details = new PlantedPlantDetails(signedPlantId, p.getName(), p.getDescription(), p.getCategories(), p.getPhotosUri(), false, new ArrayList<>());
        }

        // 2. Находим все места посадки этого растения для текущего пользователя
        List<PlotDto.Response> plots = plotService.getPlotsByUser(userId);
        for (PlotDto.Response plot : plots) {
            List<BedDto.Response> beds = bedService.getBedsByPlot(plot.getId(), userId);
            for (BedDto.Response bed : beds) {
                // ИСПРАВЛЕНИЕ: точное сравнение ID со знаком, чтобы избежать пересечений личных и справочных растений
                if (bed.getPlantId() != null && bed.getPlantId().equals(signedPlantId)) {
                    details.getLocations().add(new PlantLocation(plot.getId(), plot.getName(), "Грядка", bed.getId()));
                }
            }
            List<TreebushDto.Response> trees = treebushService.getTreebushByPlot(plot.getId(), userId);
            for (TreebushDto.Response tree : trees) {
                // ИСПРАВЛЕНИЕ: точное сравнение ID со знаком
                if (tree.getPlantId() != null && tree.getPlantId().equals(signedPlantId)) {
                    details.getLocations().add(new PlantLocation(plot.getId(), plot.getName(), "Дерево/Куст", tree.getId()));
                }
            }
        }

        model.addAttribute("plant", details);
        return "planted-plant-view";
    }
}