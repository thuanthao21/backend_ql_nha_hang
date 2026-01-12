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
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // SỬA TẠI ĐÂY: Thêm đường dẫn không có tiền tố /api nếu Frontend gọi trực tiếp
                        .requestMatchers("/api/auth/**", "/auth/**", "/ws/**").permitAll()

                        .requestMatchers("/api/orders/**").hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers("/api/tables/**").hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers("/api/products/**").hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers("/api/categories/**").hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers("/api/reports/**").hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers("/api/users/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
