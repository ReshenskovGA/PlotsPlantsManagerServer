package com.garden.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;

public class PlantDto {

    // --- Личные растения (Plant) ---
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Long id; // Добавлено для редактирования
        @NotBlank(message = "Название растения обязательно")
        private String name;
        private String description;
        private List<String> categories;
        private List<String> photosUri;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private List<String> categories;
        private List<String> photosUri;
    }

    // --- Справочник растений (PlantConst) ---
    // Для справочника обычно нужен только Response, так как клиент его только читает
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConstResponse {
        private Long id;
        private String name;
        private String description;
        private List<String> categories;
        private List<String> photosUri;
    }
}