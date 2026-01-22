package com.dineflow.backend.repository;

import com.dineflow.backend.entity.Order;
import com.dineflow.backend.entity.OrderStatus; // <--- Import Enum OrderStatus
import com.dineflow.backend.entity.RestaurantTable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // =======================================================
    // 1. CÁC HÀM CƠ BẢN (Đã chuyển từ String -> OrderStatus)
    // =======================================================

    // [MỚI] Tìm danh sách đơn theo trạng thái + Sắp xếp cũ nhất lên đầu (Dùng cho Kitchen Controller)
    List<Order> findByStatusInOrderByCreatedAtAsc(List<OrderStatus> statuses);

    // [FIX] Tìm đơn theo danh sách trạng thái (Đổi String -> OrderStatus)
    List<Order> findByStatusIn(List<OrderStatus> statuses);

    // [FIX] Tìm đơn theo bàn và trạng thái (Dùng cho Service - Đổi String -> OrderStatus)
    // Lưu ý: Tên hàm là StatusInAndTable thì tham số phải là (List Status, Table)
    Optional<Order> findByStatusInAndTable(List<OrderStatus> statuses, RestaurantTable table);

    // Tìm ngược lại (Nếu code cũ có dùng Table trước)
    Optional<Order> findByTableAndStatusIn(RestaurantTable table, List<OrderStatus> statuses);


    // =======================================================
    // 2. CÁC HÀM QUERY TÙY CHỈNH (Reports & Dashboard)
    // =======================================================

    // Tìm đơn active bằng ID bàn (JPQL)
    // Lưu ý: Sửa so sánh 'COMPLETED' thành Enum
    @Query("SELECT o FROM Order o WHERE o.table.id = :tableId AND o.status <> com.dineflow.backend.entity.OrderStatus.COMPLETED AND o.status <> com.dineflow.backend.entity.OrderStatus.CANCELLED")
    Order findActiveOrderByTableId(@Param("tableId") Integer tableId);

    // --- BÁO CÁO DOANH THU (Native Query - SQL thuần) ---
    // (Giữ nguyên nativeQuery vì SQL so sánh chuỗi trong Database vẫn đúng)

    @Query(value = """
        SELECT COALESCE(SUM(total_amount), 0) 
        FROM orders 
        WHERE created_at >= :startDate AND created_at <= :endDate 
        AND status = 'COMPLETED'
    """, nativeQuery = true)
    Double calculateRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = """
        SELECT COUNT(*) 
        FROM orders 
        WHERE created_at >= :startDate AND created_at <= :endDate
        AND status = 'COMPLETED'
    """, nativeQuery = true)
    Integer countOrders(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = """
        SELECT TO_CHAR(created_at, 'YYYY-MM-DD') as date_str, 
               COALESCE(SUM(total_amount), 0) as total 
        FROM orders 
        WHERE created_at >= :startDate AND created_at <= :endDate 
        AND status = 'COMPLETED' 
        GROUP BY TO_CHAR(created_at, 'YYYY-MM-DD') 
        ORDER BY date_str ASC
    """, nativeQuery = true)
    List<Object[]> getRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // --- TOP 5 MÓN BÁN CHẠY (JPQL) ---
    // Sửa so sánh status = 'COMPLETED' thành status = Enum
    @Query("SELECT i.product.name, " +
            "SUM(i.quantity), " +
            "SUM(i.quantity * i.priceAtPurchase) " +
            "FROM OrderItem i " +
            "WHERE i.order.createdAt >= :startDate AND i.order.createdAt <= :endDate " +
            "AND i.order.status = com.dineflow.backend.entity.OrderStatus.COMPLETED " +
            "GROUP BY i.product.name " +
            "ORDER BY SUM(i.quantity) DESC")
    List<Object[]> getTopSellingProducts(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}