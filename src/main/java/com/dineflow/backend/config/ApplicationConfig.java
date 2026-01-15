package com.dineflow.backend.config;

import com.dineflow.backend.entity.Role;
import com.dineflow.backend.entity.User;
import com.dineflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
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

    // --- SỬA ĐỔI: CHỈ TẠO ADMIN NẾU CHƯA TỒN TẠI (KHÔNG RESET MẬT KHẨU NỮA) ---
    @Bean
    public CommandLineRunner createDefaultAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            try {
                // Kiểm tra xem user "admin" đã có trong Database chưa
                if (userRepository.findByUsername("admin").isEmpty()) {

                    // Nếu chưa có -> Tạo mới với mật khẩu 123456
                    User admin = new User();
                    admin.setUsername("admin");
                    admin.setPassword(passwordEncoder.encode("123456"));
                    admin.setRole(Role.ADMIN);
                    admin.setFullName("Super Admin");

                    userRepository.save(admin);

                    System.out.println("=============================================");
                    System.out.println("✅ ĐÃ KHỞI TẠO TÀI KHOẢN ADMIN MẶC ĐỊNH");
                    System.out.println("👉 Username: admin");
                    System.out.println("👉 Password: 123456");
                    System.out.println("=============================================");
                } else {
                    // Nếu đã có -> Không làm gì cả (Giữ nguyên mật khẩu bạn đã đổi)
                    System.out.println("=============================================");
                    System.out.println("👍 Admin đã tồn tại. Bỏ qua bước reset mật khẩu.");
                    System.out.println("=============================================");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Lỗi kiểm tra admin khởi tạo: " + e.getMessage());
            }
        };
    }
}