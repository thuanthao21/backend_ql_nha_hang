package com.dineflow.backend.service;

import com.dineflow.backend.dto.DashboardDTO;
import com.dineflow.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;

    /**
     * Lấy dữ liệu dashboard theo khoảng ngày tùy chọn
     * @param fromDate yyyy-MM-dd
     * @param toDate   yyyy-MM-dd
     */
    public DashboardDTO getDashboardStats(String fromDate, String toDate) {

        // =======================
        // 1. SỐ LIỆU HÔM NAY (GIỮ NGUYÊN)
        // =======================
        Double todayRev = orderRepository.getTodayRevenue();
        Integer todayOrd = orderRepository.countTodayOrders();

        if (todayRev == null) todayRev = 0.0;
        if (todayOrd == null) todayOrd = 0;

        // =======================
        // 2. PARSE NGÀY
        // =======================
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 00:00:00 ngày bắt đầu
        LocalDateTime start = LocalDate.parse(fromDate, formatter).atStartOfDay();

        // 23:59:59 ngày kết thúc
        LocalDateTime end = LocalDate.parse(toDate, formatter).atTime(23, 59, 59);

        // =======================
        // 3. LẤY DỮ LIỆU TỪ DB
        // =======================
        List<Object[]> rawData = orderRepository.getRevenueByDateRange(start, end);

        Map<String, Double> revenueMap = rawData.stream().collect(Collectors.toMap(
                row -> row[0].toString(),               // yyyy-MM-dd
                row -> Double.parseDouble(row[1].toString())
        ));

        // =======================
        // 4. LẤP ĐẦY NGÀY TRỐNG
        // =======================
        List<DashboardDTO.ChartDataDTO> chartData = new ArrayList<>();

        LocalDate current = start.toLocalDate();
        LocalDate last = end.toLocalDate();

        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("dd/MM");

        while (!current.isAfter(last)) {

            String dbKey = current.format(formatter);       // yyyy-MM-dd
            String label = current.format(displayFormat);   // dd/MM

            chartData.add(
                    new DashboardDTO.ChartDataDTO(
                            label,
                            revenueMap.getOrDefault(dbKey, 0.0)
                    )
            );

            current = current.plusDays(1);
        }

        return new DashboardDTO(todayRev, todayOrd, chartData);
    }
}
