package com.dineflow.backend.repository;

import com.dineflow.backend.entity.Order;
import com.dineflow.backend.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // =======================
    // CÁC HÀM CŨ (GIỮ NGUYÊN)
    // =======================

    List<Order> findByStatusIn(List<String> statuses);

    Optional<Order> findByTableAndStatus(RestaurantTable table, String status);

    Optional<Order> findByTableAndStatusIn(RestaurantTable table, List<String> statuses);

    // =======================
    // THỐNG KÊ – DASHBOARD
    // =======================

    @Query(
            value = "SELECT COALESCE(SUM(o.total_amount), 0) " +
                    "FROM orders o " +
                    "WHERE o.status = 'COMPLETED' " +
                    "AND DATE(o.created_at) = CURRENT_DATE",
            nativeQuery = true
    )
    Double getTodayRevenue();

    @Query(
            value = "SELECT COUNT(*) " +
                    "FROM orders o " +
                    "WHERE o.status = 'COMPLETED' " +
                    "AND DATE(o.created_at) = CURRENT_DATE",
            nativeQuery = true
    )
    Integer countTodayOrders();

    // (Có thể giữ để backward-compatible)
    @Query(
            value = "SELECT CAST(o.created_at AS DATE) AS date, " +
                    "COALESCE(SUM(o.total_amount), 0) AS total " +
                    "FROM orders o " +
                    "WHERE o.status = 'COMPLETED' " +
                    "AND o.created_at >= CURRENT_DATE - INTERVAL '7 days' " +
                    "GROUP BY CAST(o.created_at AS DATE) " +
                    "ORDER BY date ASC",
            nativeQuery = true
    )
    List<Object[]> getRevenueLast7Days();

    // =======================
    // 🚀 QUERY MỚI – DATE RANGE (QUAN TRỌNG)
    // =======================

    /**
     * Lấy doanh thu theo từng ngày trong khoảng thời gian bất kỳ
     * Dùng cho Date Range Picker (from - to)
     *
     * Trả về: Object[] { LocalDate, Double }
     */
    @Query(
            value = "SELECT CAST(o.created_at AS DATE) AS date, " +
                    "COALESCE(SUM(o.total_amount), 0) AS total " +
                    "FROM orders o " +
                    "WHERE o.status = 'COMPLETED' " +
                    "AND o.created_at BETWEEN :startDate AND :endDate " +
                    "GROUP BY CAST(o.created_at AS DATE) " +
                    "ORDER BY date ASC",
            nativeQuery = true
    )
    List<Object[]> getRevenueByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
