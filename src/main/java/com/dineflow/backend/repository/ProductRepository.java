package com.dineflow.backend.repository;

import com.dineflow.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    // Kiểm tra xem món ăn đã có chưa
    boolean existsByName(String name);
}