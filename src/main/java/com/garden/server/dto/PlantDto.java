package com.garden.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;

public class PlantDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Long id;
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