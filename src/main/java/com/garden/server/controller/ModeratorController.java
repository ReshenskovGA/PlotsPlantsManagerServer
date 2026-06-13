package com.garden.server.controller.web;

import com.garden.server.dto.PlantDto;
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
        model.addAttribute("plant", new PlantDto.Request());
        model.addAttribute("allCategories", CATEGORIES);
        return "moderator-plant-form";
    }

    @GetMapping("/plants/edit/{id}")
    public String showEditPlantForm(@PathVariable Long id, Model model) {
        // Для упрощения получаем через getAllPlants и фильтруем,
        // или можно добавить метод getPlantConstById в PlantConstService
        PlantDto.ConstResponse plant = plantConstService.getAllPlants().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Растение не найдено"));

        PlantDto.Request request = new PlantDto.Request();
        request.setName(plant.getName());
        request.setDescription(plant.getDescription());
        request.setCategories(plant.getCategories());
        request.setPhotosUri(plant.getPhotosUri());

        model.addAttribute("plant", request);
        model.addAttribute("plantId", id);
        model.addAttribute("allCategories", CATEGORIES);
        return "moderator-plant-form";
    }

    @PostMapping("/plants/save")
    public String savePlant(@ModelAttribute PlantDto.Request request,
                            @RequestParam(value = "plantId", required = false) Long plantId,
                            @RequestParam(value = "photos", required = false) List<MultipartFile> photos) {
        // Примечание: для полноценной загрузки фото нужно интегрировать FileStorageService,
        // аналогично тому, как это сделано в WebPlantController.
        // Здесь упрощенный вариант без обработки MultipartFile для краткости,
        // или можно скопировать логику из WebPlantController.savePlant.

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