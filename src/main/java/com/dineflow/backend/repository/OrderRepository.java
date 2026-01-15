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
    // 1. CÁC HÀM LOGIC CŨ (GIỮ NGUYÊN ĐỂ KHÔNG LỖI APP)
    // =======================

    List<Order> findByStatusIn(List<String> statuses);

    Optional<Order> findByTableAndStatus(RestaurantTable table, String status);

    Optional<Order> findByTableAndStatusIn(RestaurantTable table, List<String> statuses);

    // Tìm đơn đang ăn (chưa thanh toán) của bàn
    // Logic: Bàn này có đơn nào KHÁC 'COMPLETED' và KHÁC 'CANCELLED' không?
    @Query("SELECT o FROM Order o WHERE o.table.id = :tableId AND o.status <> 'COMPLETED' AND o.status <> 'CANCELLED'")
    Order findActiveOrderByTableId(@Param("tableId") Integer tableId);

    // =======================
    // 2. QUERY BÁO CÁO MỚI (FIX TIMEZONE HEROKU)
    // Dùng: created_at và status = 'COMPLETED'
    // =======================

    /**
     * Tính tổng tiền theo khoảng thời gian (Start -> End)
     * Dùng cho Dashboard hôm nay hoặc Custom Range
     */
    @Query(value = """
        SELECT COALESCE(SUM(total_amount), 0) 
        FROM orders 
        WHERE created_at >= :startDate AND created_at <= :endDate 
        AND status = 'COMPLETED'
    """, nativeQuery = true)
    Double calculateRevenue(@Param("startDate") LocalDateTime startDate,
                            @Param("endDate") LocalDateTime endDate);

    /**
     * Đếm số đơn hàng theo khoảng thời gian
     */
    @Query(value = """
        SELECT COUNT(*) 
        FROM orders 
        WHERE created_at >= :startDate AND created_at <= :endDate
        AND status = 'COMPLETED'
    """, nativeQuery = true)
    Integer countOrders(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

    /**
     * Lấy dữ liệu vẽ biểu đồ cột
     * Group by ngày (YYYY-MM-DD)
     */
    @Query(value = """
        SELECT TO_CHAR(created_at, 'YYYY-MM-DD') as date_str, 
               COALESCE(SUM(total_amount), 0) as total 
        FROM orders 
        WHERE created_at >= :startDate AND created_at <= :endDate 
        AND status = 'COMPLETED' 
        GROUP BY TO_CHAR(created_at, 'YYYY-MM-DD') 
        ORDER BY date_str ASC
    """, nativeQuery = true)
    List<Object[]> getRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // =======================
    // 3. QUERY CŨ (BACKUP - CÓ THỂ GIỮ LẠI HOẶC KHÔNG)
    // Các hàm này dùng CURRENT_DATE của SQL (sẽ bị lệch múi giờ trên Heroku)
    // ReportService mới đã chuyển sang dùng 3 hàm ở mục 2 bên trên rồi.
    // =======================

    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status = 'COMPLETED' AND DATE(created_at) = CURRENT_DATE", nativeQuery = true)
    Double getTodayRevenue();

    @Query(value = "SELECT COUNT(*) FROM orders WHERE status = 'COMPLETED' AND DATE(created_at) = CURRENT_DATE", nativeQuery = true)
    Integer countTodayOrders();
}