package com.dineflow.backend.repository;

import com.dineflow.backend.entity.Order;
import com.dineflow.backend.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    // Tìm các đơn theo danh sách trạng thái (Dùng cho Bếp)
    List<Order> findByStatusIn(List<String> statuses);

    // Tìm đơn cũ: Chỉ tìm đúng 1 trạng thái
    Optional<Order> findByTableAndStatus(RestaurantTable table, String status);

    // --- THÊM HÀM NÀY ---
    // Tìm đơn của bàn đang ngồi (chấp nhận cả UNPAID và PENDING)
    Optional<Order> findByTableAndStatusIn(RestaurantTable table, List<String> statuses);
}