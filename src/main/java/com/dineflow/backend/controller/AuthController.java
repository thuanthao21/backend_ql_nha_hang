package com.dineflow.backend.controller;

import com.dineflow.backend.dto.AuthResponse;
import com.dineflow.backend.dto.LoginRequest;
import com.dineflow.backend.entity.User;
import com.dineflow.backend.repository.UserRepository;
import com.dineflow.backend.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Log kiểm tra xem request có đến được đây không
            System.out.println("👉 Đang nhận yêu cầu login cho user: " + request.getUsername());

            // 1. Xác thực username/password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 2. Nếu đúng, tìm user trong DB
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // 3. Sinh token
            String token = jwtService.generateToken(user);
            System.out.println("✅ Login thành công. Role: " + user.getRole().name());

            // 4. Trả về token và role
            return ResponseEntity.ok(new AuthResponse(token, user.getRole().name()));

        } catch (BadCredentialsException e) {
            System.err.println("❌ Sai mật khẩu hoặc username cho user: " + request.getUsername());
            return ResponseEntity.status(401).body("Sai tên đăng nhập hoặc mật khẩu");
        } catch (Exception e) {
            System.err.println("❌ Lỗi hệ thống khi login: " + e.getMessage());
            e.printStackTrace(); // In chi tiết lỗi ra log
            return ResponseEntity.badRequest().body("Lỗi đăng nhập: " + e.getMessage());
        }
    }
}