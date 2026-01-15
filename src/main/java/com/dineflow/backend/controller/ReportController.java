package com.dineflow.backend.controller;

import com.dineflow.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        // Logic mặc định 7 ngày nếu không truyền tham số
        if (from == null || from.isEmpty() || to == null || to.isEmpty()) {
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysAgo = today.minusDays(6);
            from = sevenDaysAgo.toString();
            to = today.toString();
        }

        // Validate ngày (đổi chỗ nếu user nhập ngược)
        if (LocalDate.parse(from).isAfter(LocalDate.parse(to))) {
            String temp = from;
            from = to;
            to = temp;
        }

        // --- SỬA LỖI TẠI ĐÂY ---
        return ResponseEntity.ok()
                // 1. Chuẩn HTTP 1.1 (Hiện đại)
                .cacheControl(CacheControl.noStore().mustRevalidate())

                // 2. Chuẩn HTTP 1.0 (Cũ nhưng cần thiết cho trình duyệt cũ)
                .header("Pragma", "no-cache")

                // 3. Ép hết hạn ngay lập tức
                .header("Expires", "0")

                .body(reportService.getDashboardStats(from, to));
    }
}