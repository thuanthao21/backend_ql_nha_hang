package com.dineflow.backend.config;

import com.dineflow.backend.entity.Role; // Import Role Enum
import com.dineflow.backend.entity.User; // Import User Entity
import com.dineflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner; // Import CommandLineRunner
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // --- PHẦN THÊM MỚI: TỰ ĐỘNG TẠO USER ADMIN CHUẨN ---
    @Bean
    public CommandLineRunner createDefaultAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            try {
                // Kiểm tra xem admin đã có chưa, nếu chưa thì tạo mới
                User admin = userRepository.findByUsername("admin")
                        .orElse(new User());

                // Cập nhật lại thông tin chuẩn (đè lên dữ liệu cũ nếu có sai sót)
                admin.setUsername("admin");
                // Mã hóa mật khẩu 123456 chuẩn BCrypt
                admin.setPassword(passwordEncoder.encode("123456"));
                // Set Role Enum chuẩn (tránh lỗi String/Enum không khớp)
                admin.setRole(Role.ADMIN);
                admin.setFullName("Super Admin");

                userRepository.save(admin);

                System.out.println("=============================================");
                System.out.println("✅ ĐÃ RESET TÀI KHOẢN ADMIN THÀNH CÔNG!");
                System.out.println("👉 Username: admin");
                System.out.println("👉 Password: 123456");
                System.out.println("👉 Role: ADMIN");
                System.out.println("=============================================");
            } catch (Exception e) {
                System.err.println("⚠️ Không thể tạo admin tự động: " + e.getMessage());
            }
        };
    }
}