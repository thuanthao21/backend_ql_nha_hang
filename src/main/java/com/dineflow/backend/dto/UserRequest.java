package com.dineflow.backend.dto;
import com.dineflow.backend.entity.Role;
import lombok.Data;

@Data
public class UserRequest {
    private String username;
    private String fullName;
    private Role role;
    private String password; // Chỉ dùng khi tạo mới
}