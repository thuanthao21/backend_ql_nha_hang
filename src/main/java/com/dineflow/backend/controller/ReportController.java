package com.dineflow.backend.controller;

import com.dineflow.backend.entity.Order;
import com.dineflow.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final OrderRepository orderRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        List<Order> allOrders = orderRepository.findAll();

        // 1. Tính tổng doanh thu (chỉ tính đơn đã hoàn thành COMPLETED)
        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()) && o.getTotalAmount() != null)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Đếm số đơn hôm nay
        long todayOrders = allOrders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                .count();

        // 3. Mock data biểu đồ (Giả lập doanh thu 7 ngày để vẽ cho đẹp)
        // Trong thực tế sẽ dùng SQL Group By
        Object[] chartData = new Object[]{
                Map.of("date", "20/11", "value", 1500000),
                Map.of("date", "21/11", "value", 2300000),
                Map.of("date", "22/11", "value", 1800000),
                Map.of("date", "23/11", "value", 3200000),
                Map.of("date", "24/11", "value", 2100000),
                Map.of("date", "25/11", "value", 1200000),
                Map.of("date", "Hôm nay", "value", totalRevenue) // Số thực tế
        };

        Map<String, Object> response = new HashMap<>();
        response.put("revenue", totalRevenue);
        response.put("todayOrders", todayOrders);
        response.put("chartData", chartData);

        return ResponseEntity.ok(response);
    }
}