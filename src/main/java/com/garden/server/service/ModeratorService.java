package com.garden.server.service;

import com.garden.server.entity.User;
import com.garden.server.repository.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModeratorService {

    private final UserRepository userRepository;
    private final PlantRepository plantRepository;
    private final PlotRepository plotRepository;
    private final BedRepository bedRepository;
    private final TreebushRepository treebushRepository;
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public List<UserStatsDto> getAllUsersStats() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> {
            Long userId = user.getId();
            return new UserStatsDto(
                    user.getId(),
                    user.getUsername(),
                    user.getLogin(),
                    user.getPassword(), // Хэш пароля (BCrypt)
                    user.getEmail(),
                    user.getRole(),
                    plantRepository.countByUserId(userId),
                    plotRepository.countByUserId(userId),
                    bedRepository.countByUserId(userId) + treebushRepository.countByUserId(userId),
                    taskRepository.countByUserId(userId)
            );
        }).collect(Collectors.toList());
    }

    @Data
    @AllArgsConstructor
    public static class UserStatsDto {
        private Long id;
        private String username;
        private String login;
        private String passwordHash;
        private String email;
        private String role;
        private long plantsCount;
        private long plotsCount;
        private long gardenItemsCount; // Грядки + Деревья/Кусты
        private long tasksCount;
    }
}