package com.garden.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garden.server.dto.*;
import com.garden.server.enums.EntityType;
import com.garden.server.enums.OperationType;
import com.garden.server.enums.SyncStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final ObjectMapper objectMapper;
    private final PlotService plotService;
    private final BedService bedService;
    private final TreebushService treebushService;
    private final TaskService taskService;
    private final PlantService plantService;

    public SyncDto.SyncResponse processSync(SyncDto.SyncRequest request, Long userId) {
        List<SyncDto.OperationResult> results = new ArrayList<>();

        for (SyncDto.OperationRequest op : request.getOperations()) {
            SyncDto.OperationResult result = SyncDto.OperationResult.builder()
                    .entityId(op.getEntityId())
                    .entityType(op.getEntityType())
                    .operationType(op.getOperationType())
                    .build();

            try {
                switch (op.getEntityType()) {
                    case PLOT:
                        handlePlotSync(op, userId, result);
                        break;
                    case BED:
                        handleBedSync(op, userId, result);
                        break;
                    case TASK:
                        handleTaskSync(op, userId, result);
                        break;
                    case TREEBUSH: handleTreebushSync(op, userId, result); break;
                    case PLANT: handlePlantSync(op, userId, result); break;
                    default:
                        throw new IllegalArgumentException("Неподдерживаемый тип сущности: " + op.getEntityType());
                }
                result.setStatus(SyncStatus.SYNCED);
            } catch (Exception e) {
                log.error("Ошибка синхронизации сущности {} типа {}: {}", op.getEntityId(), op.getEntityType(), e.getMessage());
                result.setStatus(SyncStatus.FAILED);
                result.setErrorMessage(e.getMessage());
            }
            results.add(result);
        }

        return SyncDto.SyncResponse.builder().results(results).build();
    }

    private void handleTaskSync(SyncDto.OperationRequest op, Long userId, SyncDto.OperationResult result) throws Exception {
        TaskDto.Request dto = objectMapper.readValue(op.getPayloadJson(), TaskDto.Request.class);
        if (op.getOperationType() == OperationType.CREATE) {
            TaskDto.Response saved = taskService.createTask(dto, userId);
            result.setServerId(saved.getId());
        } else if (op.getOperationType() == OperationType.UPDATE) {
            if (taskService.existsByIdAndUser(op.getEntityId(), userId)) {
                taskService.updateTask(op.getEntityId(), dto, userId);
            } else {
                result.setStatus(SyncStatus.FAILED);
                result.setErrorMessage("Задача не существует");
            }
        } else if (op.getOperationType() == OperationType.DELETE) {
            if (taskService.existsByIdAndUser(op.getEntityId(), userId)) {
                taskService.deleteTask(op.getEntityId(), userId);
            } else {
                result.setStatus(SyncStatus.SYNCED);
            }
        }
    }

    private void handlePlotSync(SyncDto.OperationRequest op, Long userId, SyncDto.OperationResult result) throws Exception {
        PlotDto.Request dto = objectMapper.readValue(op.getPayloadJson(), PlotDto.Request.class);
        if (op.getOperationType() == OperationType.CREATE) {
            PlotDto.Response saved = plotService.createPlot(dto, userId);
            result.setServerId(saved.getId());
        } else if (op.getOperationType() == OperationType.UPDATE) {
            if (plotService.existsByIdAndUser(op.getEntityId(), userId)) {
                plotService.updatePlot(op.getEntityId(), dto, userId);
            } else {
                result.setStatus(SyncStatus.FAILED);
                result.setErrorMessage("Участок не существует");
            }
        } else if (op.getOperationType() == OperationType.DELETE) {
            if (plotService.existsByIdAndUser(op.getEntityId(), userId)) {
                plotService.deletePlot(op.getEntityId(), userId);
            } else {
                result.setStatus(SyncStatus.SYNCED);
            }
        }
    }

    private void handleBedSync(SyncDto.OperationRequest op, Long userId, SyncDto.OperationResult result) throws Exception {
        BedDto.Request dto = objectMapper.readValue(op.getPayloadJson(), BedDto.Request.class);
        if (op.getOperationType() == OperationType.CREATE) {
            BedDto.Response saved = bedService.createBed(dto, userId);
            result.setServerId(saved.getId());
        } else if (op.getOperationType() == OperationType.UPDATE) {
            if (bedService.existsByIdAndUser(op.getEntityId(), userId)) {
                bedService.updateBed(op.getEntityId(), dto, userId);
            } else {
                result.setStatus(SyncStatus.FAILED);
                result.setErrorMessage("Грядка не существует");
            }
        } else if (op.getOperationType() == OperationType.DELETE) {
            if (bedService.existsByIdAndUser(op.getEntityId(), userId)) {
                bedService.deleteBed(op.getEntityId(), userId);
            } else {
                result.setStatus(SyncStatus.SYNCED);
            }
        }
    }

    private void handleTreebushSync(SyncDto.OperationRequest op, Long userId, SyncDto.OperationResult result) throws Exception {
        TreebushDto.Request dto = objectMapper.readValue(op.getPayloadJson(), TreebushDto.Request.class);
        if (op.getOperationType() == OperationType.CREATE) {
            TreebushDto.Response saved = treebushService.createTreebush(dto, userId);
            result.setServerId(saved.getId());
        } else if (op.getOperationType() == OperationType.UPDATE) {
            if (treebushService.existsByIdAndUser(op.getEntityId(), userId)) {
                treebushService.updateTreebush(op.getEntityId(), dto, userId);
            } else {
                result.setStatus(SyncStatus.FAILED);
                result.setErrorMessage("Дерево/куст не существует");
            }
        } else if (op.getOperationType() == OperationType.DELETE) {
            if (treebushService.existsByIdAndUser(op.getEntityId(), userId)) {
                treebushService.deleteTreebush(op.getEntityId(), userId);
            } else {
                result.setStatus(SyncStatus.SYNCED);
            }
        }
    }

    private void handlePlantSync(SyncDto.OperationRequest op, Long userId, SyncDto.OperationResult result) throws Exception {
        PlantDto.Request dto = objectMapper.readValue(op.getPayloadJson(), PlantDto.Request.class);
        if (op.getOperationType() == OperationType.CREATE) {
            PlantDto.Response saved = plantService.createPlant(dto, userId);
            result.setServerId(saved.getId());
        } else if (op.getOperationType() == OperationType.UPDATE) {
            if (plantService.existsByIdAndUser(op.getEntityId(), userId)) {
                plantService.updatePlant(op.getEntityId(), dto, userId);
            } else {
                result.setStatus(SyncStatus.FAILED);
                result.setErrorMessage("Растение не существует");
            }
        } else if (op.getOperationType() == OperationType.DELETE) {
            if (plantService.existsByIdAndUser(op.getEntityId(), userId)) {
                plantService.deletePlant(op.getEntityId(), userId);
            } else {
                result.setStatus(SyncStatus.SYNCED);
            }
        }
    }
}