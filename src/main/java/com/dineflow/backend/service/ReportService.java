package com.dineflow.backend.service;

import com.dineflow.backend.dto.DashboardDTO;
import com.dineflow.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest; // 👈 Import PageRequest
import org.springframework.data.domain.Pageable;   // 👈 Import Pageable
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

    // --- HÀM 1: DASHBOARD ---
    public DashboardDTO getDashboardStats(String fromDate, String toDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 1. XỬ LÝ MÚI GIỜ
        ZoneId vnZone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate todayVN = LocalDate.now(vnZone);

        LocalDateTime startToday = todayVN.atStartOfDay();
        LocalDateTime endToday = todayVN.atTime(LocalTime.MAX);

        Double todayRev = orderRepository.calculateRevenue(startToday, endToday);
        Integer todayOrd = orderRepository.countOrders(startToday, endToday);

        if (todayRev == null) todayRev = 0.0;
        if (todayOrd == null) todayOrd = 0;

        // 2. XỬ LÝ BIỂU ĐỒ
        LocalDateTime startChart = LocalDate.parse(fromDate, formatter).atStartOfDay();
        LocalDateTime endChart = LocalDate.parse(toDate, formatter).atTime(LocalTime.MAX);

        List<Object[]> rawData = orderRepository.getRevenueByDateRange(startChart, endChart);

        Map<String, Double> revenueMap = rawData.stream().collect(Collectors.toMap(
                row -> row[0].toString(),
                row -> Double.parseDouble(row[1].toString())
        ));

        List<DashboardDTO.ChartDataDTO> chartData = new ArrayList<>();
        LocalDate current = startChart.toLocalDate();
        LocalDate last = endChart.toLocalDate();
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("dd/MM");

        while (!current.isAfter(last)) {
            String dbKey = current.format(formatter);
            String label = current.format(displayFormat);

            chartData.add(new DashboardDTO.ChartDataDTO(
                    label,
                    revenueMap.getOrDefault(dbKey, 0.0)
            ));

            current = current.plusDays(1);
        }

        return new DashboardDTO(todayRev, todayOrd, chartData);
    }

    // --- HÀM 2: TOP SẢN PHẨM (MỚI THÊM) ---
    public List<TopProductDTO> getTopProducts(String fromDate, String toDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime start = LocalDate.parse(fromDate, formatter).atStartOfDay();
        LocalDateTime end = LocalDate.parse(toDate, formatter).atTime(LocalTime.MAX);

        // Tạo Pageable để lấy 5 dòng đầu tiên
        Pageable topFive = PageRequest.of(0, 5);

        List<Object[]> rawData = orderRepository.getTopSellingProducts(start, end, topFive);

        // Convert Object[] -> DTO
        return rawData.stream().map(row -> new TopProductDTO(
                (String) row[0],                        // Tên món
                Long.parseLong(row[1].toString()),      // Số lượng
                Double.parseDouble(row[2].toString())   // Doanh thu
        )).collect(Collectors.toList());
    }

    // DTO nội bộ
    public record TopProductDTO(String name, Long quantity, Double revenue) {}
}