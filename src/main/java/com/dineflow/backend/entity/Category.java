package com.dineflow.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer id;

    @Column(name = "name", length = 100)
    private String name;

    // Đệ quy: Danh mục cha
    @ManyToOne(fetch = FetchType.LAZY) // Chỉ load khi cần để nhẹ dữ liệu
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "parentCategory"}) // Chặn vòng lặp khi convert JSON
    private Category parentCategory;
}