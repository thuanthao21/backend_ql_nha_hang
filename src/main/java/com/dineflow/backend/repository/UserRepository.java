package com.dineflow.backend.repository;

import com.dineflow.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    // Tìm user bằng username (trả về Optional để tránh lỗi Null)
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}