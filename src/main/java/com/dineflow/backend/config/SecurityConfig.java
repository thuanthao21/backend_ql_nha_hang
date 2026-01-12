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
                // 1. Tắt CSRF và cấu hình CORS
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // 2. Cấu hình phân quyền
                .authorizeHttpRequests(auth -> auth
                        // Cho phép tất cả các request OPTIONS (Preflight request của trình duyệt)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Mở công khai các đường dẫn đăng nhập và websocket
                        // Dùng AntPathRequestMatcher ngầm định để khớp chính xác
                        .requestMatchers("/auth/**", "/api/auth/**", "/ws/**").permitAll()

                        // Phân quyền cho các API chức năng
                        .requestMatchers("/api/orders/**", "/api/tables/**", "/api/products/**",
                                "/api/categories/**", "/api/reports/**").hasAnyAuthority("ADMIN", "STAFF")

                        .requestMatchers("/api/users/**").hasAuthority("ADMIN")

                        // Tất cả các yêu cầu khác phải được xác thực
                        .anyRequest().authenticated()
                )

                // 3. Quản lý Session là Stateless (không lưu session trên server)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Cấu hình Provider và Filter
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}