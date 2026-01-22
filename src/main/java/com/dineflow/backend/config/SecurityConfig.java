package com.dineflow.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
                .csrf(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        // 1. Cho phép tất cả các request OPTIONS (Fix lỗi CORS Preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Mở toang các đường dẫn Public (Login, Socket, Ảnh...)
                        .requestMatchers("/auth/**", "/api/auth/**", "/ws/**", "/login/**").permitAll()

                        // =================================================================
                        // 3. PHÂN QUYỀN CHỨC NĂNG (ĐÃ SỬA)
                        // =================================================================

                        // [QUAN TRỌNG] Bếp cần quyền vào: Đơn hàng (để nấu), Bàn (để xem tên bàn), Sản phẩm (để xem tên món)
                        .requestMatchers("/api/orders/**", "/api/tables/**", "/api/products/**")
                        .hasAnyAuthority("ADMIN", "STAFF", "KITCHEN") // <--- THÊM "KITCHEN" VÀO ĐÂY

                        // Các mục quản lý khác (Danh mục, Báo cáo) -> Bếp không cần vào
                        .requestMatchers("/api/categories/**", "/api/reports/**")
                        .hasAnyAuthority("ADMIN", "STAFF")

                        // Quản lý nhân viên -> Chỉ Admin
                        .requestMatchers("/api/users/**").hasAuthority("ADMIN")

                        // 4. Tất cả các request còn lại phải đăng nhập
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}