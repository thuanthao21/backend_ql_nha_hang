package com.dineflow.backend.controller;

import com.dineflow.backend.dto.AuthResponse;
import com.dineflow.backend.dto.LoginRequest;
import com.dineflow.backend.entity.User;
import com.dineflow.backend.repository.UserRepository;
import com.dineflow.backend.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // 1. Xác thực username/password (Spring Security tự làm)
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

        // 4. Trả về token và role
        return ResponseEntity.ok(new AuthResponse(token, user.getRole().name()));
    }
}