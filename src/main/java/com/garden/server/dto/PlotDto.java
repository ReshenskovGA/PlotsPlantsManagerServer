package com.garden.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.util.List;

public class PlotDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Long id;
        @NotBlank(message = "Название участка обязательно")
        private String name;

        private String address;
        private String cadastralNumber;

        @NotNull(message = "Длина обязательна")
        @Positive(message = "Длина должна быть больше 0")
        private Double length;

        @NotNull(message = "Ширина обязательна")
        @Positive(message = "Ширина должна быть больше 0")
        private Double width;

        private List<String> photosUri;
        private String planPhotoUri;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long userId;
        private String name;
        private String address;
        private String cadastralNumber;
        private Double length;
        private Double width;
        private List<String> photosUri;
        private String planPhotoUri;
    }
}