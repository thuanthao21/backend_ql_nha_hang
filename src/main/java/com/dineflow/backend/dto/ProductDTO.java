package com.dineflow.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {

    // Không cần ID khi tạo mới, nhưng cần khi update/hiển thị
    private Integer id;

    @NotBlank(message = "Tên món không được để trống")
    private String name;

    @NotNull(message = "Giá tiền không được để trống")
    @Min(value = 0, message = "Giá tiền phải lớn hơn 0")
    private BigDecimal price;

    private String imageUrl;

    private String kitchenStation; // 'BAR' hoặc 'KITCHEN'

    // Đây là điểm quan trọng: Chúng ta chỉ nhận ID của danh mục thôi
    private Integer categoryId;

    // Khi trả về, có thể kèm thêm tên danh mục cho tiện hiển thị
    private String categoryName;

    private Boolean isAvailable;
}