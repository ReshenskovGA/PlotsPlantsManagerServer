package com.garden.server.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                boolean isModerator = authentication.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_MODERATOR".equals(a.getAuthority()));

                if (isModerator) {
                    getRedirectStrategy().sendRedirect(request, response, "/web/moderator/users");
                } else {
                    getRedirectStrategy().sendRedirect(request, response, "/web/dashboard");
                }
            }
        };
        handler.setAlwaysUseDefaultTargetUrl(true);
        return handler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/v1/**", "/web/login"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/web/login", "/web/register",
                                "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                        .requestMatchers("/api/v1/auth/**", "/api/v1/plants-const/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        .requestMatchers("/web/moderator/**").hasRole("MODERATOR")

                        .requestMatchers("/web/**").authenticated()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/web/login")
                        .loginProcessingUrl("/web/login")
                        .usernameParameter("login")
                        .passwordParameter("password")

                        .successHandler(successHandler())
                        .failureUrl("/web/login?error=true")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("GardenAppUniqueSecretKey_2026")
                        .tokenValiditySeconds(86400)
                        .rememberMeParameter("remember-me")
                )
                .logout(logout -> logout
                        .logoutUrl("/web/logout")
                        .logoutSuccessUrl("/?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                );
        return http.build();
    }
}