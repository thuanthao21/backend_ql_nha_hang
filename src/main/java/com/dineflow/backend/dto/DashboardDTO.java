package com.dineflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDTO {
    private Double todayRevenue;    // Doanh thu hôm nay
    private Integer todayOrders;    // Số đơn hôm nay
    private List<ChartDataDTO> chartData; // Dữ liệu biểu đồ 7 ngày

    @Data
    @AllArgsConstructor
    public static class ChartDataDTO {
        private String date;   // Ngày (VD: "25/10")
        private Double value;  // Tiền (VD: 500000)
    }
}