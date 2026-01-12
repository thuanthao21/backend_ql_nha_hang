package com.dineflow.backend.controller;

import com.dineflow.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Lấy dữ liệu dashboard theo khoảng ngày tùy chọn
     * @param from yyyy-MM-dd
     * @param to   yyyy-MM-dd
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        // Nếu không truyền ngày, mặc định 7 ngày gần nhất
        if (from == null || from.isEmpty() || to == null || to.isEmpty()) {
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysAgo = today.minusDays(6);
            from = sevenDaysAgo.toString();
            to = today.toString();
        }

        // Optionally: Validate từ ngày không được sau đến ngày
        if (LocalDate.parse(from).isAfter(LocalDate.parse(to))) {
            // Hoán đổi tự động
            String temp = from;
            from = to;
            to = temp;
        }

        return ResponseEntity.ok(reportService.getDashboardStats(from, to));
    }
}
