package com.dineflow.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore; // ✅ QUAN TRỌNG: Chống lỗi vòng lặp
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore // ✅ Dòng này giúp sửa lỗi 400 Bad Request khi lấy API /current
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price_at_purchase")
    private BigDecimal priceAtPurchase;

    @Column(name = "note")
    private String note;

    // ✅ Giữ là String để dễ lưu các trạng thái: PENDING, COOKING, READY, SERVED
    @Column(name = "status", length = 20)
    private String status = "PENDING";
}