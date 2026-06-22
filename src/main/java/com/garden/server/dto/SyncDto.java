package com.garden.server.dto;

import com.garden.server.enums.EntityType;
import com.garden.server.enums.OperationType;
import com.garden.server.enums.SyncStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

public class SyncDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OperationRequest {
        @NotNull(message = "ID сущности обязателен")
        private Long entityId;

        @NotNull(message = "Тип сущности обязателен")
        private EntityType entityType;

        @NotNull(message = "Тип операции обязателен")
        private OperationType operationType;

        @NotNull(message = "Payload (данные) обязателен")
        private String payloadJson;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SyncRequest {
        @NotEmpty(message = "Список операций не может быть пустым")
        private List<OperationRequest> operations;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OperationResult {
        private Long entityId;
        private EntityType entityType;
        private OperationType operationType;
        private SyncStatus status;
        private String errorMessage;
        private Long serverId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SyncResponse {
        private List<OperationResult> results;
    }
}