package com.dineflow.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredient_id")
    private Integer id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "quantity_in_stock")
    private BigDecimal quantityInStock; // Số lượng tồn kho (VD: 10.5 kg)

    @Column(name = "unit", length = 20)
    private String unit; // Đơn vị tính: kg, gram, lít, hộp...
}