package com.garden.server.service;

import com.garden.server.dto.TreebushDto;
import com.garden.server.entity.Treebush;
import com.garden.server.entity.Plot;
import com.garden.server.entity.User;
import com.garden.server.repository.TreebushRepository;
import com.garden.server.repository.PlotRepository;
import com.garden.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TreebushService {

    private final TreebushRepository treebushRepository;
    private final PlotRepository plotRepository;
    private final UserRepository userRepository;

    @Transactional
    public TreebushDto.Response createTreebush(TreebushDto.Request request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        Plot plot = plotRepository.findById(request.getPlotId())
                .orElseThrow(() -> new IllegalArgumentException("Участок не найден"));

        if (!plot.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен: участок принадлежит другому пользователю");
        }

        Treebush treebush = Treebush.builder()
                .user(user)
                .plot(plot)
                .plantId(request.getPlantId())
                .diameter(request.getDiameter())
                .offsetX(request.getOffsetX())
                .offsetY(request.getOffsetY())
                .markerColor(request.getMarkerColor())
                .build();

        return mapToResponse(treebushRepository.save(treebush));
    }

    @Transactional(readOnly = true)
    public TreebushDto.Response getTreebushById(Long treebushId, Long userId) {
        Treebush treebush = treebushRepository.findById(treebushId)
                .orElseThrow(() -> new IllegalArgumentException("Дерево/Куст не найдены"));
        if (!treebush.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен");
        }
        return mapToResponse(treebush);
    }

    @Transactional(readOnly = true)
    public List<TreebushDto.Response> getTreebushByPlot(Long plotId, Long userId) {
        Plot plot = plotRepository.findById(plotId)
                .orElseThrow(() -> new IllegalArgumentException("Участок не найден"));
        if (!plot.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен");
        }
        return treebushRepository.findByPlotId(plotId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public TreebushDto.Response updateTreebush(Long treebushId, TreebushDto.Request request, Long userId) {
        Treebush treebush = treebushRepository.findById(treebushId)
                .orElseThrow(() -> new IllegalArgumentException("Грядка не найдена"));

        if (!treebush.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен");
        }

        // При обновлении участка проверяем, что новый участок тоже принадлежит пользователю
        if (!treebush.getPlot().getId().equals(request.getPlotId())) {
            Plot newPlot = plotRepository.findById(request.getPlotId())
                    .orElseThrow(() -> new IllegalArgumentException("Новый участок не найден"));
            if (!newPlot.getUser().getId().equals(userId)) {
                throw new SecurityException("Доступ запрещен к новому участку");
            }
            treebush.setPlot(newPlot);
        }

        treebush.setPlantId(request.getPlantId());
        treebush.setDiameter(request.getDiameter());
        treebush.setOffsetX(request.getOffsetX());
        treebush.setOffsetY(request.getOffsetY());
        treebush.setMarkerColor(request.getMarkerColor());

        return mapToResponse(treebushRepository.save(treebush));
    }

    @Transactional
    public void deleteTreebush(Long treebushId, Long userId) {
        Treebush treebush = treebushRepository.findById(treebushId)
                .orElseThrow(() -> new IllegalArgumentException("Грядка не найдена"));
        if (!treebush.getUser().getId().equals(userId)) {
            throw new SecurityException("Доступ запрещен");
        }
        treebushRepository.delete(treebush);
    }

    private TreebushDto.Response mapToResponse(Treebush treebush) {
        return TreebushDto.Response.builder()
                .id(treebush.getId())
                .plotId(treebush.getPlot().getId())
                .userId(treebush.getUser().getId())
                .plantId(treebush.getPlantId())
                .diameter(treebush.getDiameter())
                .offsetX(treebush.getOffsetX())
                .offsetY(treebush.getOffsetY())
                .markerColor(treebush.getMarkerColor())
                .build();
    }
}