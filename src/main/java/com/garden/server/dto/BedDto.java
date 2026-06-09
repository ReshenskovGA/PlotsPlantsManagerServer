package com.garden.server.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

public class BedDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Long id; // Добавлено для редактирования
        @NotNull(message = "ID участка обязателен")
        private Long plotId;
        private Long plantId;
        @NotNull(message = "Длина обязательна")
        @Positive(message = "Длина должна быть больше 0")
        private Double length;
        @NotNull(message = "Ширина обязательна")
        @Positive(message = "Ширина должна быть больше 0")
        private Double width;
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
        private Double length;
        private Double width;
        private Double offsetX;
        private Double offsetY;
        private Integer markerColor;
    }
}