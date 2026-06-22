package com.garden.server.controller.web;

import com.garden.server.dto.PlantDto;
import com.garden.server.service.FileStorageService;
import com.garden.server.service.ModeratorService;
import com.garden.server.service.PlantConstService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/web/moderator")
@RequiredArgsConstructor
public class ModeratorController {

    private final ModeratorService moderatorService;
    private final PlantConstService plantConstService;
    private final FileStorageService fileStorageService;

    private static final List<String> CATEGORIES = Arrays.asList(
            "Дерево", "Кустарник", "Многолетнее травянистое", "Однолетнее", "Двухлетнее",
            "Овощная культура", "Плодово-ягодная", "Пряная / Зелень", "Декоративная",
            "Цветущая", "Луковичное / Клубневое", "Вьющееся / Лиана", "Хвойное",
            "Лиственное", "Суккулент", "Комнатное растение", "Лекарственное"
    );

    @GetMapping("/users")
    public String viewUsersStats(Model model) {
        model.addAttribute("usersStats", moderatorService.getAllUsersStats());
        return "moderator-users";
    }

    @GetMapping("/plants")
    public String viewCatalogPlants(Model model) {
        model.addAttribute("plants", plantConstService.getAllPlants());
        return "moderator-plants";
    }

    @GetMapping("/plants/add")
    public String showAddPlantForm(Model model) {
        PlantDto.Request request = new PlantDto.Request();
        request.setPhotosUri(new ArrayList<>());
        model.addAttribute("plant", request);
        model.addAttribute("allCategories", CATEGORIES);
        return "moderator-plant-form";
    }

    @GetMapping("/plants/edit/{id}")
    public String showEditPlantForm(@PathVariable Long id, Model model) {
        PlantDto.ConstResponse plant = plantConstService.getAllPlants().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Растение не найдено"));

        PlantDto.Request request = new PlantDto.Request();
        request.setName(plant.getName());
        request.setDescription(plant.getDescription());
        request.setCategories(plant.getCategories());
        request.setPhotosUri(plant.getPhotosUri() != null
                ? new ArrayList<>(plant.getPhotosUri())
                : new ArrayList<>());

        model.addAttribute("plant", request);
        model.addAttribute("plantId", id);
        model.addAttribute("allCategories", CATEGORIES);
        return "moderator-plant-form";
    }

    @PostMapping("/plants/save")
    public String savePlant(@ModelAttribute PlantDto.Request request,
                            @RequestParam(value = "plantId", required = false) Long plantId,
                            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
                            @RequestParam(value = "keptPhotos", required = false) List<String> keptPhotos) {

        // 1. Собираем итоговый список фото: те, что остались отмеченными
        List<String> finalPhotos = new ArrayList<>();
        if (keptPhotos != null) {
            finalPhotos.addAll(keptPhotos);
        }

        // 2. При редактировании — удаляем с диска те фото, которые сняли с галочки
        if (plantId != null) {
            PlantDto.ConstResponse existing = plantConstService.getAllPlants().stream()
                    .filter(p -> p.getId().equals(plantId))
                    .findFirst()
                    .orElse(null);
            if (existing != null && existing.getPhotosUri() != null) {
                List<String> toRemove = new ArrayList<>(existing.getPhotosUri());
                toRemove.removeAll(finalPhotos);
                for (String removed : toRemove) {
                    fileStorageService.deleteFile(removed);
                }
            }
        }

        // 3. Сохраняем новые загруженные фото
        if (photos != null && !photos.isEmpty()) {
            List<String> newNames = fileStorageService.storeFiles(photos);
            finalPhotos.addAll(newNames);
        }

        request.setPhotosUri(finalPhotos);

        if (plantId != null) {
            plantConstService.updatePlantConst(plantId, request);
        } else {
            plantConstService.createPlantConst(request);
        }
        return "redirect:/web/moderator/plants";
    }

    @PostMapping("/plants/delete/{id}")
    public String deletePlant(@PathVariable Long id) {
        plantConstService.deletePlantConst(id);
        return "redirect:/web/moderator/plants";
    }
}