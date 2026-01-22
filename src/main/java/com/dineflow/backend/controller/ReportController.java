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

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // Helper: Xử lý ngày mặc định
    private String[] validateDates(String from, String to) {
        if (from == null || from.isEmpty() || to == null || to.isEmpty()) {
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysAgo = today.minusDays(6);
            return new String[]{sevenDaysAgo.toString(), today.toString()};
        }
        if (LocalDate.parse(from).isAfter(LocalDate.parse(to))) {
            return new String[]{to, from}; // Đảo ngược
        }
        return new String[]{from, to};
    }

    // API Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats(@RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        String[] dates = validateDates(from, to);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore().mustRevalidate())
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(reportService.getDashboardStats(dates[0], dates[1]));
    }

    // [MỚI] API Top Products
    @GetMapping("/top-products")
    public ResponseEntity<?> getTopProducts(@RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        String[] dates = validateDates(from, to);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore().mustRevalidate())
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(reportService.getTopProducts(dates[0], dates[1]));
    }
}