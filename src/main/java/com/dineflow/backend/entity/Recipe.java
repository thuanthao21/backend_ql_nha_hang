package com.dineflow.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id")
    private Integer id;

    // Món nào?
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Nguyên liệu gì?
    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    // Cần bao nhiêu?
    @Column(name = "quantity_required")
    private BigDecimal quantityRequired;
}