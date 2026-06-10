package com.garden.server.service;

import com.garden.server.dto.BedDto;
import com.garden.server.entity.Bed;
import com.garden.server.entity.Plot;
import com.garden.server.entity.User;
import com.garden.server.repository.BedRepository;
import com.garden.server.repository.PlotRepository;
import com.garden.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BedService {

    private final BedRepository bedRepository;
    private final PlotRepository plotRepository;
    private final UserRepository userRepository;

    @Transactional
    public BedDto.Response createBed(BedDto.Request request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        Plot plot = plotRepository.findById(request.getPlotId())
                .orElseThrow(() -> new IllegalArgumentException("Участок не найден"));

        if (!plot.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен: участок принадлежит другому пользователю");
        }

        Bed bed = Bed.builder()
                .user(user)
                .plot(plot)
                .plantId(request.getPlantId())
                .length(request.getLength())
                .width(request.getWidth())
                .offsetX(request.getOffsetX())
                .offsetY(request.getOffsetY())
                .markerColor(request.getMarkerColor())
                .build();

        return mapToResponse(bedRepository.save(bed));
    }

    @Transactional(readOnly = true)
    public List<BedDto.Response> getBedsByPlot(Long plotId, Long userId) {
        Plot plot = plotRepository.findById(plotId)
                .orElseThrow(() -> new IllegalArgumentException("Участок не найден"));
        if (!plot.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен");
        }
        return bedRepository.findByPlotId(plotId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public BedDto.Response updateBed(Long bedId, BedDto.Request request, Long userId) {
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new IllegalArgumentException("Грядка не найдена"));

        if (!bed.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен");
        }

        // При обновлении участка проверяем, что новый участок тоже принадлежит пользователю
        if (!bed.getPlot().getId().equals(request.getPlotId())) {
            Plot newPlot = plotRepository.findById(request.getPlotId())
                    .orElseThrow(() -> new IllegalArgumentException("Новый участок не найден"));
            if (!newPlot.getUser().getId().equals(userId)) {
                throw new SecurityException("Доступ запрещен к новому участку");
            }
            bed.setPlot(newPlot);
        }

        bed.setPlantId(request.getPlantId());
        bed.setLength(request.getLength());
        bed.setWidth(request.getWidth());
        bed.setOffsetX(request.getOffsetX());
        bed.setOffsetY(request.getOffsetY());
        bed.setMarkerColor(request.getMarkerColor());

        return mapToResponse(bedRepository.save(bed));
    }

    @Transactional(readOnly = true)
    public BedDto.Response getBedById(Long bedId, Long userId) {
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new IllegalArgumentException("Грядка не найдена"));
        if (!bed.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен");
        }
        return mapToResponse(bed);
    }

    @Transactional
    public void deleteBed(Long bedId, Long userId) {
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new IllegalArgumentException("Грядка не найдена"));
        if (!bed.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен");
        }
        bedRepository.delete(bed);
    }

    private BedDto.Response mapToResponse(Bed bed) {
        return BedDto.Response.builder()
                .id(bed.getId())
                .plotId(bed.getPlot().getId())
                .userId(bed.getUser().getId())
                .plantId(bed.getPlantId())
                .length(bed.getLength())
                .width(bed.getWidth())
                .offsetX(bed.getOffsetX())
                .offsetY(bed.getOffsetY())
                .markerColor(bed.getMarkerColor())
                .build();
    }

    @Transactional(readOnly = true)
    public List<BedDto.Response> getBedsByUser(Long userId) {
        return bedRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}