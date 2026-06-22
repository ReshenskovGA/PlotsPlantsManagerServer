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

        for (PlantDto.ConstResponse p : plantConstService.getAllPlants()) {
            items.add(new PlantListItem(p.getId(), p.getName(), p.getDescription(), p.getCategories(), p.getPhotosUri(), true));
        }
        for (PlantDto.Response p : plantService.getPlantsByUser(userId)) {
            items.add(new PlantListItem(p.getId(), p.getName(), p.getDescription(), p.getCategories(), p.getPhotosUri(), false));
        }

        if (search != null && !search.trim().isEmpty()) {
            String lowerSearch = search.toLowerCase();
            items.removeIf(p -> !p.getName().toLowerCase().contains(lowerSearch));
        }
        if (source != null && !source.equals("all")) {
            boolean isCatalog = source.equals("catalog");
            items.removeIf(p -> p.getCatalog() != isCatalog);
        }
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
                            @RequestParam(value = "keptPhotos", required = false) List<String> keptPhotos,
                            Authentication auth) {
        Long userId = getCurrentUserId(auth);

        List<String> finalPhotos = new ArrayList<>();
        if (keptPhotos != null) {
            finalPhotos.addAll(keptPhotos);
        }

        if (request.getId() != null) {
            PlantDto.Response existingPlant = plantService.getPlantById(request.getId(), userId);
            if (existingPlant.getPhotosUri() != null) {
                List<String> photosToRemove = new ArrayList<>(existingPlant.getPhotosUri());
                photosToRemove.removeAll(finalPhotos);
                for (String removed : photosToRemove) {
                    fileStorageService.deleteFile(removed);
                }
            }
        }

        List<String> newPhotoNames = fileStorageService.storeFiles(photos);
        finalPhotos.addAll(newPhotoNames);
        request.setPhotosUri(finalPhotos);

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