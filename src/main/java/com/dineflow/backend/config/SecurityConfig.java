package com.dineflow.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource)) // Cấu hình CORS
                .authorizeHttpRequests(auth -> auth
                        // 1. Cho phép tất cả các request OPTIONS (Frontend hỏi đường)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Các API công khai (Login, WebSocket)
                        .requestMatchers("/api/auth/**", "/ws/**").permitAll()

                        // 3. Các API nghiệp vụ (Dành cho cả ADMIN và STAFF)
                        // Bao gồm: Gọi món, Bếp, Xem bàn, Xem thực đơn
                        // Dấu ** ở cuối nghĩa là bao gồm tất cả link con (VD: /items/{id}/status)
                        .requestMatchers("/api/orders/**").hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers("/api/tables/**").hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers("/api/products/**").hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers("/api/categories/**").hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers("/api/reports/**").hasAnyAuthority("ADMIN", "STAFF")

                        // 4. Các API quản trị (Chỉ ADMIN được dùng)
                        // Ví dụ: Quản lý nhân viên
                        .requestMatchers("/api/users/**").hasAuthority("ADMIN")

                        // 5. Tất cả request còn lại phải đăng nhập
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}