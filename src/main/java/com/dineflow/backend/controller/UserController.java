package com.dineflow.backend.controller;

import com.dineflow.backend.entity.Role;
import com.dineflow.backend.entity.User;
import com.dineflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. Lấy danh sách nhân viên (Chỉ Admin được xem)
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // 2. Tạo nhân viên mới
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("Tên đăng nhập đã tồn tại!");
        }

        // Mặc định pass là 123456 nếu không nhập, hoặc lấy pass từ request
        String rawPassword = (user.getPassword() == null || user.getPassword().isEmpty()) ? "123456" : user.getPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));

        // Mặc định role là STAFF nếu không chọn
        if (user.getRole() == null) {
            user.setRole(Role.STAFF);
        }

        return ResponseEntity.ok(userRepository.save(user));
    }

    // 3. Xóa nhân viên
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("Đã xóa nhân viên!");
    }
}