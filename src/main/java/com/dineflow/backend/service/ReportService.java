package com.dineflow.backend.service;

import com.dineflow.backend.dto.DashboardDTO;
import com.dineflow.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;

    public DashboardDTO getDashboardStats(String fromDate, String toDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 1. XỬ LÝ MÚI GIỜ (QUAN TRỌNG TRÊN HEROKU)
        // Heroku chạy UTC, ta phải ép về giờ Việt Nam để tính "Hôm nay"
        ZoneId vnZone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate todayVN = LocalDate.now(vnZone);

        // Bắt đầu ngày hôm nay: 00:00:00 (Giờ VN)
        LocalDateTime startToday = todayVN.atStartOfDay();
        // Kết thúc ngày hôm nay: 23:59:59.9999 (Giờ VN)
        LocalDateTime endToday = todayVN.atTime(LocalTime.MAX);

        // Gọi Repo với thời gian chính xác
        Double todayRev = orderRepository.calculateRevenue(startToday, endToday);
        Integer todayOrd = orderRepository.countOrders(startToday, endToday);

        // 2. XỬ LÝ BIỂU ĐỒ (CHART)
        // Parse ngày từ request (Front gửi lên yyyy-MM-dd)
        LocalDateTime startChart = LocalDate.parse(fromDate, formatter).atStartOfDay();
        LocalDateTime endChart = LocalDate.parse(toDate, formatter).atTime(LocalTime.MAX);

        List<Object[]> rawData = orderRepository.getRevenueByDateRange(startChart, endChart);

        // Chuyển List Object[] thành Map để dễ tra cứu
        Map<String, Double> revenueMap = rawData.stream().collect(Collectors.toMap(
                row -> row[0].toString(),
                row -> Double.parseDouble(row[1].toString())
        ));

        // Tạo danh sách đầy đủ các ngày (kể cả ngày không có doanh thu -> điền 0)
        List<DashboardDTO.ChartDataDTO> chartData = new ArrayList<>();
        LocalDate current = startChart.toLocalDate();
        LocalDate last = endChart.toLocalDate();
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("dd/MM");

        while (!current.isAfter(last)) {
            String dbKey = current.format(formatter);       // yyyy-MM-dd (Key trong Map)
            String label = current.format(displayFormat);   // dd/MM (Hiển thị biểu đồ)

            chartData.add(new DashboardDTO.ChartDataDTO(
                    label,
                    revenueMap.getOrDefault(dbKey, 0.0)
            ));

            current = current.plusDays(1);
        }

        return new DashboardDTO(todayRev, todayOrd, chartData);
    }
}