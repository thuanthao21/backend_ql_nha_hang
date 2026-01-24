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
                        // 1. Fix lỗi CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Mở các đường dẫn Public cơ bản
                        .requestMatchers("/auth/**", "/api/auth/**", "/ws/**", "/login/**").permitAll()

                        // =================================================================
                        // [FIX QUAN TRỌNG] CHO PHÉP KHÁCH HÀNG XEM MENU
                        // =================================================================
                        // Cho phép TẤT CẢ mọi người (bao gồm khách chưa đăng nhập) được phép XEM (GET) danh sách:
                        // - Món ăn (products)
                        // - Danh mục (categories)
                        // - Thông tin bàn (tables - để check bàn trống/có người)
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/tables/**").permitAll()

                        // =================================================================
                        // 3. PHÂN QUYỀN CHỨC NĂNG QUẢN LÝ (Cần đăng nhập)
                        // =================================================================

                        // Các thao tác THAY ĐỔI dữ liệu (POST, PUT, DELETE) trên Products, Tables thì mới cần quyền
                        .requestMatchers("/api/products/**", "/api/tables/**")
                        .hasAnyAuthority("ADMIN", "STAFF", "KITCHEN")

                        // Đơn hàng (Orders):
                        // Lưu ý: Nếu khách hàng tự bấm nút "Đặt món" (Tạo đơn), bạn cũng cần mở POST /api/orders/create cho public
                        // hoặc dùng logic riêng. Đoạn dưới đây áp dụng cho quản lý đơn hàng.
                        .requestMatchers("/api/orders/**")
                        .hasAnyAuthority("ADMIN", "STAFF", "KITCHEN")

                        // Các mục quản lý khác
                        .requestMatchers("/api/categories/**", "/api/reports/**")
                        .hasAnyAuthority("ADMIN", "STAFF")

                        // Quản lý nhân viên
                        .requestMatchers("/api/users/**").hasAuthority("ADMIN")

                        // 4. Còn lại bắt buộc đăng nhập
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}