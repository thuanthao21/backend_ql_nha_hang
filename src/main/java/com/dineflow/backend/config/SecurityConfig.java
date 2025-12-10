package com.dineflow.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <--- Nhớ import dòng này
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configure(http))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/public/**", "/ws/**").permitAll()

                        // --- SỬA ĐOẠN NÀY ---
                        // Cho phép GET (xem danh mục) với mọi user đã đăng nhập (Staff, Admin, thậm chí user thường)
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").authenticated()

                        // Các hành động sửa/xóa/thêm thì mới cần quyền cụ thể (Nếu muốn chặt chẽ)
                        .requestMatchers(HttpMethod.POST, "/api/categories/**").hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasAnyAuthority("ADMIN", "STAFF")

                        // API Orders
                        .requestMatchers("/api/orders/**").hasAnyAuthority("ADMIN", "STAFF")

                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}