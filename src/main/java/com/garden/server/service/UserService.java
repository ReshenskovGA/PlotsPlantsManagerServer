package com.garden.server.service;

import com.garden.server.dto.UserDto;
import com.garden.server.entity.User;
import com.garden.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // private final JwtTokenProvider jwtTokenProvider; // Раскомментируйте при реализации JWT

    @Transactional
    public UserDto.Response register(UserDto.RegisterRequest request) {
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        User user = User.builder()
                .login(request.getLogin())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .username(request.getUsername())
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDto.AuthResponse login(UserDto.LoginRequest request) {
        User user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new IllegalArgumentException("Неверный логин или пароль"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }

        // String token = jwtTokenProvider.generateToken(user.getLogin());
        String token = "mock-jwt-token-for-development"; // Заглушка до реализации JWT

        return UserDto.AuthResponse.builder()
                .token(token)
                .user(mapToResponse(user))
                .build();
    }

    @Transactional(readOnly = true)
    public UserDto.Response getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        return mapToResponse(user);
    }

    private UserDto.Response mapToResponse(User user) {
        return UserDto.Response.builder()
                .id(user.getId())
                .login(user.getLogin())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }
}