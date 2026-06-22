package com.garden.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class TaskDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Long id;

        private Long plotId;

        @NotBlank(message = "Название задачи обязательно")
        private String title;

        private String description;

        @NotNull(message = "Дата выполнения обязательна")
        private Long date;

        @NotNull(message = "Статус выполнения обязателен")
        private Boolean isCompleted;

        private String recurrenceRuleJson;
        private String categoryName;
        private Long plantedItemId;
        private String plantedItemType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long userId;
        private Long plotId;
        private String title;
        private String description;
        private Long date;
        private Boolean isCompleted;
        private String recurrenceRuleJson;
        private String categoryName;
        private Long plantedItemId;
        private String plantedItemType;
    }
}