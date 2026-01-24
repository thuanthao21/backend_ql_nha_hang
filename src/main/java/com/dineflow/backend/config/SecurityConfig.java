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
                        // 1. Fix lỗi CORS Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Mở các đường dẫn Public (Auth, Socket...)
                        .requestMatchers("/auth/**", "/api/auth/**", "/ws/**", "/login/**").permitAll()

                        // =================================================================
                        // [FIX 1] CHO PHÉP KHÁCH XEM MENU & BÀN (GET)
                        // =================================================================
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/tables/**").permitAll()

                        // =================================================================
                        // [FIX 2 - QUAN TRỌNG] CHO PHÉP KHÁCH ĐẶT MÓN (POST)
                        // =================================================================
                        // Cho phép TẤT CẢ mọi người (bao gồm khách) được GỬI đơn hàng mới.
                        // Lưu ý: Chỉ mở quyền POST (Tạo mới), không mở PUT/DELETE (Sửa/Xóa).
                        .requestMatchers(HttpMethod.POST, "/api/orders/**").permitAll()

                        // =================================================================
                        // 3. PHÂN QUYỀN CHỨC NĂNG QUẢN LÝ (Cần đăng nhập)
                        // =================================================================

                        // Các thao tác QUẢN LÝ (Sửa, Xóa, Cập nhật trạng thái) thì phải là Nhân viên
                        // Cụ thể: PUT (Sửa trạng thái món), DELETE (Xóa món/đơn)
                        .requestMatchers(HttpMethod.PUT, "/api/orders/**", "/api/products/**", "/api/tables/**")
                        .hasAnyAuthority("ADMIN", "STAFF", "KITCHEN")

                        .requestMatchers(HttpMethod.DELETE, "/api/orders/**", "/api/products/**", "/api/tables/**")
                        .hasAnyAuthority("ADMIN", "STAFF", "KITCHEN")

                        // Xem danh sách đơn hàng (GET /api/orders) thường là Staff xem, nhưng nếu khách cần xem đơn của chính họ
                        // thì logic API phải tự filter. Tạm thời nếu đây là endpoint quản lý, hãy giữ quyền Staff.
                        // Nếu khách cần xem lại đơn vừa đặt, bạn có thể tách API riêng hoặc mở luôn GET (nhưng rủi ro lộ đơn bàn khác).
                        // Tạm thời mở quyền GET cho STAFF xem tất cả.
                        .requestMatchers(HttpMethod.GET, "/api/orders/**").hasAnyAuthority("ADMIN", "STAFF", "KITCHEN")

                        // Thêm/Sửa sản phẩm, bàn (POST cho Admin/Staff/Kitchen nhập liệu)
                        .requestMatchers(HttpMethod.POST, "/api/products/**", "/api/tables/**")
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