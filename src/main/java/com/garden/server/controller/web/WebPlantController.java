package com.garden.server.controller.web;

import com.garden.server.dto.PlantDto;
import com.garden.server.entity.Plant;
import com.garden.server.entity.PlantConst;
import com.garden.server.entity.User;
import com.garden.server.repository.PlantConstRepository;
import com.garden.server.repository.PlantRepository;
import com.garden.server.repository.UserRepository;
import com.garden.server.service.FileStorageService;
import com.garden.server.service.PlantConstService;
import com.garden.server.service.PlantService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/web/plants")
@RequiredArgsConstructor
public class WebPlantController {
    private final PlantService plantService;
    private final PlantConstService plantConstService;
    private final PlantConstRepository plantConstRepository;
    private final PlantRepository plantRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    private Long getCurrentUserId(Authentication authentication) {
        String login = authentication.getName();
        return userRepository.findByLogin(login)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден в сессии"));
    }

    private static final List<String> CATEGORIES = Arrays.asList(
            "Дерево", "Кустарник", "Многолетнее травянистое", "Однолетнее", "Двухлетнее",
            "Овощная культура", "Плодово-ягодная", "Пряная / Зелень", "Декоративная",
            "Цветущая", "Луковичное / Клубневое", "Вьющееся / Лиана", "Хвойное",
            "Лиственное", "Суккулент", "Комнатное растение", "Лекарственное"
    );

    // Внутренний класс для объединения данных из справочника и личных растений
    @Data
    @AllArgsConstructor
    public static class PlantListItem {
        private Long id;
        private String name;
        private String description;
        private List<String> categories;
        private List<String> photosUri;
        private Boolean catalog;

    }

    @GetMapping
    public String listPlants(Authentication auth,
                             @RequestParam(required = false) String search,
                             @RequestParam(required = false) String source,
                             @RequestParam(required = false) List<String> categories, Model model) {
        Long userId = getCurrentUserId(auth);
        List<PlantListItem> items = new ArrayList<>();

        // Добавляем растения из справочника
        for (PlantDto.ConstResponse p : plantConstService.getAllPlants()) {
            items.add(new PlantListItem(p.getId(), p.getName(), p.getDescription(), p.getCategories(), p.getPhotosUri(), true));
        }
        // Добавляем личные растения
        for (PlantDto.Response p : plantService.getPlantsByUser(userId)) {
            items.add(new PlantListItem(p.getId(), p.getName(), p.getDescription(), p.getCategories(), p.getPhotosUri(), false));
        }

        // Фильтрация по поиску
        if (search != null && !search.trim().isEmpty()) {
            String lowerSearch = search.toLowerCase();
            items.removeIf(p -> !p.getName().toLowerCase().contains(lowerSearch));
        }
        // Фильтрация по источнику
        if (source != null && !source.equals("all")) {
            boolean isCatalog = source.equals("catalog");
            // ИСПРАВЛЕНИЕ: используем getCatalog() вместо isCatalog()
            items.removeIf(p -> p.getCatalog() != isCatalog);
        }
        // Фильтрация по категориям
        if (categories != null && !categories.isEmpty()) {
            items.removeIf(p -> p.getCategories() == null ||
                    p.getCategories().stream().noneMatch(categories::contains));
        }

        model.addAttribute("plants", items);
        model.addAttribute("search", search);
        model.addAttribute("source", source);
        model.addAttribute("selectedCategories", categories);
        model.addAttribute("allCategories", CATEGORIES);
        return "plants";
    }

    // Просмотр детальной информации о растении
    @GetMapping("/view/{id}")
    public String viewPlant(@PathVariable Long id, @RequestParam String source, Model model) {
        PlantListItem item;
        if ("catalog".equals(source)) {
            PlantConst plant = plantConstRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Растение не найдено"));
            item = new PlantListItem(plant.getId(), plant.getName(), plant.getDescription(), plant.getCategories(), plant.getPhotosUri(), true);
        } else {
            Plant plant = plantRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Растение не найдено"));
            item = new PlantListItem(plant.getId(), plant.getName(), plant.getDescription(), plant.getCategories(), plant.getPhotosUri(), false);
        }
        model.addAttribute("plant", item);
        return "plant-view";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("plant", new PlantDto.Request());
        model.addAttribute("allCategories", CATEGORIES);
        return "plant-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Authentication auth, Model model) {
        Long userId = getCurrentUserId(auth);
        PlantDto.Response plant = plantService.getPlantById(id, userId);
        model.addAttribute("plant", plant);
        model.addAttribute("allCategories", CATEGORIES);
        return "plant-form";
    }

    @PostMapping("/save")
    public String savePlant(@ModelAttribute PlantDto.Request request,
                            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
                            Authentication auth) {
        Long userId = getCurrentUserId(auth);
        List<String> photoNames = fileStorageService.storeFiles(photos);
        if (request.getPhotosUri() != null) {
            photoNames.addAll(0, request.getPhotosUri());
        }
        request.setPhotosUri(photoNames);

        if (request.getId() != null) {
            plantService.updatePlant(request.getId(), request, userId);
        } else {
            plantService.createPlant(request, userId);
        }
        return "redirect:/web/plants";
    }

    @PostMapping("/delete/{id}")
    public String deletePlant(@PathVariable Long id, Authentication auth) {
        Long userId = getCurrentUserId(auth);
        plantService.deletePlant(id, userId);
        return "redirect:/web/plants";
    }
}