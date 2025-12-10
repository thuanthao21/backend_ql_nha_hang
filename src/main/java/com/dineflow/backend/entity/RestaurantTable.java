package com.dineflow.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private Integer id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "status", length = 20)
    private String status = "EMPTY"; // Mặc định là bàn trống

    // Nhiều bàn thuộc 1 khu vực
    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;
}