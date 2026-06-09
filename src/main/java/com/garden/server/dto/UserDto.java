package com.garden.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class UserDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {
        @NotBlank(message = "Логин обязателен")
        @Size(min = 3, max = 50, message = "Логин должен быть от 3 до 50 символов")
        private String login;

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 5, message = "Пароль должен быть не менее 5 символов")
        private String password;

        @Email(message = "Некорректный формат email")
        private String email;

        @Size(max = 100)
        private String username;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequest {
        @NotBlank(message = "Логин обязателен")
        private String login;

        @NotBlank(message = "Пароль обязателен")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String login;
        private String email;
        private String username;
        // Поле password намеренно отсутствует для безопасности
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {
        private String token; // JWT токен
        private Response user;
    }
}