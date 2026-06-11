package com.garden.server.service;

import com.garden.server.dto.PlotDto;
import com.garden.server.entity.Plot;
import com.garden.server.entity.User;
import com.garden.server.repository.PlotRepository;
import com.garden.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlotService {
    private final PlotRepository plotRepository;
    private final UserRepository userRepository;

    @Transactional
    public PlotDto.Response createPlot(PlotDto.Request request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        Plot plot = Plot.builder()
                .user(user)
                .name(request.getName())
                .address(request.getAddress())
                .cadastralNumber(request.getCadastralNumber())
                .length(request.getLength())
                .width(request.getWidth())
                .photosUri(request.getPhotosUri())
                .planPhotoUri(request.getPlanPhotoUri())
                .build();
        return mapToResponse(plotRepository.save(plot));
    }

    @Transactional(readOnly = true)
    public List<PlotDto.Response> getPlotsByUser(Long userId) {
        return plotRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // НОВЫЙ МЕТОД: Получение участка по ID с проверкой прав
    @Transactional(readOnly = true)
    public PlotDto.Response getPlotById(Long plotId, Long userId) {
        Plot plot = plotRepository.findById(plotId)
                .orElseThrow(() -> new IllegalArgumentException("Участок не найден"));
        if (!plot.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен: участок принадлежит другому пользователю");
        }
        return mapToResponse(plot);
    }

    @Transactional
    public PlotDto.Response updatePlot(Long plotId, PlotDto.Request request, Long userId) {
        Plot plot = plotRepository.findById(plotId)
                .orElseThrow(() -> new IllegalArgumentException("Участок не найден"));
        if (!plot.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен: участок принадлежит другому пользователю");
        }
        plot.setName(request.getName());
        plot.setAddress(request.getAddress());
        plot.setCadastralNumber(request.getCadastralNumber());
        plot.setLength(request.getLength());
        plot.setWidth(request.getWidth());
        plot.setPhotosUri(request.getPhotosUri());
        plot.setPlanPhotoUri(request.getPlanPhotoUri());
        return mapToResponse(plotRepository.save(plot));
    }

    @Transactional
    public void deletePlot(Long plotId, Long userId) {
        Plot plot = plotRepository.findById(plotId)
                .orElseThrow(() -> new IllegalArgumentException("Участок не найден"));
        if (!plot.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен");
        }
        plotRepository.delete(plot);
    }

    private PlotDto.Response mapToResponse(Plot plot) {
        return PlotDto.Response.builder()
                .id(plot.getId())
                .userId(plot.getUser().getId())
                .name(plot.getName())
                .address(plot.getAddress())
                .cadastralNumber(plot.getCadastralNumber())
                .length(plot.getLength())
                .width(plot.getWidth())
                .photosUri(plot.getPhotosUri())
                .planPhotoUri(plot.getPlanPhotoUri())
                .build();
    }

    @Transactional(readOnly = true)
    public boolean existsByIdAndUser(Long plotId, Long userId) {
        return plotRepository.findById(plotId)
                .map(plot -> plot.getUser().getId().equals(userId))
                .orElse(false);
    }
}