package com.garden.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/v1/**", "/web/login"))
                .authorizeHttpRequests(auth -> auth
                        // Разрешаем доступ к главной странице, логину и статическим ресурсам
                        .requestMatchers("/", "/index.html", "/web/login", "/web/register", "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                        // Публичные REST API
                        .requestMatchers("/api/v1/auth/**", "/api/v1/plants-const/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        .requestMatchers("/web/moderator/**").hasRole("MODERATOR")
                        // Веб-страницы и REST API требуют авторизации
                        .requestMatchers("/web/**").authenticated()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/web/login")
                        .loginProcessingUrl("/web/login")
                        .usernameParameter("login")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/web/dashboard", true) // Перенаправление в дашборд после успешного входа
                        .failureUrl("/web/login?error=true")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("GardenAppUniqueSecretKey_2026") // Секретный ключ для подписи cookie (придумайте свой)
                        .tokenValiditySeconds(86400)          // 24 часа в секундах (24 * 60 * 60)
                        .rememberMeParameter("remember-me")   // Имя параметра, которое будет приходить из HTML формы
                )
                .logout(logout -> logout
                        .logoutUrl("/web/logout")
                        .logoutSuccessUrl("/?logout=true")
                        .invalidateHttpSession(true)
                        // ИСПРАВЛЕНИЕ: При выходе нужно удалять не только сессионную cookie, но и remember-me
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                );
        return http.build();
    }
}