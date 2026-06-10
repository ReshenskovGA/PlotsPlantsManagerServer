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
import com.garden.server.service.FileStorageService;
import com.garden.server.service.PlotService;
import com.garden.server.service.TreebushService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Controller
@RequestMapping("/web/plots")
@RequiredArgsConstructor
public class WebPlotController {
    private final PlotService plotService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final BedService bedService;
    private final TreebushService treebushService;
    private final PlantRepository plantRepository;
    private final PlantConstRepository plantConstRepository;

    private Long getCurrentUserId(Authentication authentication) {
        String login = authentication.getName();
        return userRepository.findByLogin(login)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден в сессии"));
    }

    // Преобразование Integer-цвета в HEX-строку для CSS
    private String intToHexColor(Integer color) {
        if (color == null) return "#9E9E9E";
        return String.format("#%06X", (0xFFFFFF & color));
    }

    @GetMapping
    public String listPlots(Authentication auth, @RequestParam(required = false) String search, Model model) {
        Long userId = getCurrentUserId(auth);
        List<PlotDto.Response> plots = plotService.getPlotsByUser(userId);
        if (search != null && !search.trim().isEmpty()) {
            plots = plots.stream()
                    .filter(p -> p.getName().toLowerCase().contains(search.toLowerCase()) ||
                            (p.getAddress() != null && p.getAddress().toLowerCase().contains(search.toLowerCase())))
                    .toList();
        }
        model.addAttribute("plots", plots);
        model.addAttribute("search", search);
        return "plots";
    }

    @GetMapping("/{id}")
    public String plotDetails(@PathVariable Long id, Authentication auth, Model model) {
        Long userId = getCurrentUserId(auth);
        PlotDto.Response plot = plotService.getPlotById(id, userId);
        List<BedDto.Response> beds = bedService.getBedsByPlot(id, userId);
        List<TreebushDto.Response> trees = treebushService.getTreebushByPlot(id, userId);

        // Собираем ID всех растений для получения их названий
        Set<Long> plantIds = new HashSet<>();
        beds.forEach(b -> { if (b.getPlantId() != null) plantIds.add(b.getPlantId()); });
        trees.forEach(t -> { if (t.getPlantId() != null) plantIds.add(t.getPlantId()); });

        Map<Long, String> plantNames = new HashMap<>();
        for (Long plantId : plantIds) {
            if (plantId < 0) {
                plantConstRepository.findById(Math.abs(plantId)).ifPresent(p -> plantNames.put(plantId, p.getName()));
            } else {
                plantRepository.findById(plantId).ifPresent(p -> plantNames.put(plantId, p.getName()));
            }
        }

        // === НОВОЕ: Мапы HEX-цветов для каждого объекта ===
        Map<Long, String> bedColors = new HashMap<>();
        beds.forEach(b -> bedColors.put(b.getId(), intToHexColor(b.getMarkerColor())));

        Map<Long, String> treeColors = new HashMap<>();
        trees.forEach(t -> treeColors.put(t.getId(), intToHexColor(t.getMarkerColor())));

        model.addAttribute("plot", plot);
        model.addAttribute("beds", beds);
        model.addAttribute("trees", trees);
        model.addAttribute("plantNames", plantNames);
        model.addAttribute("bedColors", bedColors);
        model.addAttribute("treeColors", treeColors);
        return "plot-details";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("plot", new PlotDto.Request());
        return "plot-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Authentication auth, Model model) {
        Long userId = getCurrentUserId(auth);
        PlotDto.Response plot = plotService.getPlotById(id, userId);
        model.addAttribute("plot", plot);
        return "plot-form";
    }

    @PostMapping("/save")
    public String savePlot(@ModelAttribute PlotDto.Request request,
                           @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
                           @RequestParam(value = "planPhoto", required = false) MultipartFile planPhoto,
                           @RequestParam(value = "keptPhotos", required = false) List<String> keptPhotos,
                           @RequestParam(value = "removePlanPhoto", required = false) boolean removePlanPhoto,
                           Authentication auth) {
        Long userId = getCurrentUserId(auth);

        // 1. Формируем итоговый список из отмеченных фотографий
        List<String> finalPhotos = new ArrayList<>();
        if (keptPhotos != null) {
            finalPhotos.addAll(keptPhotos);
        }

        // 2. Если редактирование - удаляем с диска неотмеченные фото
        if (request.getId() != null) {
            PlotDto.Response existingPlot = plotService.getPlotById(request.getId(), userId);
            if (existingPlot.getPhotosUri() != null) {
                List<String> photosToRemove = new ArrayList<>(existingPlot.getPhotosUri());
                photosToRemove.removeAll(finalPhotos);
                for (String removed : photosToRemove) {
                    fileStorageService.deleteFile(removed);
                }
            }

            // Обработка плана
            if (removePlanPhoto && existingPlot.getPlanPhotoUri() != null) {
                fileStorageService.deleteFile(existingPlot.getPlanPhotoUri());
                request.setPlanPhotoUri(null);
            } else if (!removePlanPhoto) {
                request.setPlanPhotoUri(existingPlot.getPlanPhotoUri());
            }
        }

        // 3. Добавляем новые загруженные фото
        List<String> newPhotoNames = fileStorageService.storeFiles(photos);
        finalPhotos.addAll(newPhotoNames);
        request.setPhotosUri(finalPhotos);

        // 4. Новый план участка
        if (planPhoto != null && !planPhoto.isEmpty()) {
            if (request.getId() != null && request.getPlanPhotoUri() != null) {
                fileStorageService.deleteFile(request.getPlanPhotoUri());
            }
            List<String> planNames = fileStorageService.storeFiles(List.of(planPhoto));
            if (!planNames.isEmpty()) {
                request.setPlanPhotoUri(planNames.get(0));
            }
        }

        if (request.getId() != null) {
            plotService.updatePlot(request.getId(), request, userId);
        } else {
            plotService.createPlot(request, userId);
        }
        return "redirect:/web/plots";
    }

    @PostMapping("/delete/{id}")
    public String deletePlot(@PathVariable Long id, Authentication auth) {
        Long userId = getCurrentUserId(auth);
        plotService.deletePlot(id, userId);
        return "redirect:/web/plots";
    }
}