package com.garden.server.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

public class TreebushDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Long id; // Добавлено для редактирования
        @NotNull(message = "ID участка обязателен")
        private Long plotId;
        private Long plantId;
        @NotNull(message = "Диаметр обязателен")
        @Positive(message = "Диаметр должен быть больше 0")
        private Double diameter;
        @NotNull(message = "Смещение X обязательно")
        private Double offsetX;
        @NotNull(message = "Смещение Y обязательно")
        private Double offsetY;
        private Integer markerColor;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long plotId;
        private Long userId;
        private Long plantId;
        private Double diameter;
        private Double offsetX;
        private Double offsetY;
        private Integer markerColor;
    }
}