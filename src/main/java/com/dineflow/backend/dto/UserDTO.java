// File: com.dineflow.backend.dto.UserDTO.java
package com.dineflow.backend.dto;
import com.dineflow.backend.entity.Role;
import lombok.Data;

@Data
public class UserDTO {
    private Integer id;
    private String username;
    private String fullName;
    private Role role;
    private boolean active;
}

