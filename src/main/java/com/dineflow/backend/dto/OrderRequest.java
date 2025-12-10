package com.dineflow.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    @NotNull(message = "ID bàn không được để trống")
    private Integer tableId;

    @NotEmpty(message = "Danh sách món ăn không được để trống")
    @Valid // Để nó kiểm tra tiếp bên trong từng món
    private List<OrderItemDTO> items;
}