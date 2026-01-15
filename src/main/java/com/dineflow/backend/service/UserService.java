package com.dineflow.backend.service;

import com.dineflow.backend.dto.UserDTO;
import com.dineflow.backend.dto.UserRequest;
import com.dineflow.backend.entity.Role;
import com.dineflow.backend.entity.User;
import com.dineflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Convert Entity -> DTO (Để ẩn password)
    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        return dto;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public UserDTO createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setRole(request.getRole() != null ? request.getRole() : Role.STAFF);
        user.setPassword(passwordEncoder.encode("123456")); // Mặc định
        user.setActive(true);
        return mapToDTO(userRepository.save(user));
    }

    public UserDTO updateUser(Integer id, UserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User không tồn tại"));
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());
        return mapToDTO(userRepository.save(user));
    }

    // Khóa hoặc Mở khóa tài khoản
    public void toggleUserStatus(Integer id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User không tồn tại"));
        user.setActive(!user.isActive()); // Đảo ngược trạng thái
        userRepository.save(user);
    }

    // Reset mật khẩu về 123456
    public void resetPassword(Integer id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User không tồn tại"));
        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);
    }

    // [MỚI] Xóa vĩnh viễn (Có kiểm tra ràng buộc dữ liệu)
    public void deleteUserPermanently(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Nhân viên không tồn tại!");
        }
        try {
            userRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // Lỗi này xảy ra khi ID nhân viên đang dính líu đến bảng khác (ví dụ: Orders)
            throw new RuntimeException("Không thể xóa nhân viên này vì họ đã có dữ liệu đơn hàng. Vui lòng sử dụng chức năng KHÓA tài khoản!");
        }
    }
}