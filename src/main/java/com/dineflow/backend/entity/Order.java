package com.dineflow.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer id;

    // Ngồi bàn nào?
    @ManyToOne
    @JoinColumn(name = "table_id")
    private RestaurantTable table;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

//    @Column(name = "status", length = 20)
//    private String status = "PENDING"; // PENDING, COMPLETED, CANCELLED
    @Enumerated(EnumType.STRING) // Lưu vào database dưới dạng chữ (ví dụ: "PENDING")
    @Column(name = "status", length = 20)
    private OrderStatus status = OrderStatus.PENDING;
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now(); // Tự động lấy giờ hiện tại

    // Một đơn hàng có nhiều món (Order Items)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;
}