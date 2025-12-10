package com.dineflow.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore; // <-- Import cái này
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "areas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Area {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "area_id")
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // Một khu vực có nhiều bàn
    @OneToMany(mappedBy = "area", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<RestaurantTable> tables;
}