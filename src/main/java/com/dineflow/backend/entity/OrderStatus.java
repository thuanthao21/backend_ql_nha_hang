package com.dineflow.backend.entity;

public enum OrderStatus {
    PENDING,    // Mới đặt, chờ bếp nhận
    COOKING,    // Bếp đang nấu
    READY,      // Bếp làm xong, chờ bưng
    SERVED,     // Đã mang ra bàn
    UNPAID,     // Khách đang ăn (Đã nhận đủ món nhưng chưa trả tiền)
    COMPLETED,  // Đã thanh toán xong
    CANCELLED   // Đã hủy
}