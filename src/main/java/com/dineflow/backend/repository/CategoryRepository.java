package com.dineflow.backend.repository;

import com.dineflow.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // Tìm danh mục theo tên để tránh tạo trùng
    Category findByName(String name);
}