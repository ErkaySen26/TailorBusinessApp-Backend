package erdalguda.main.controller;

import erdalguda.main.service.ReportingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportingService reportingService;
    
    @Autowired
    public ReportController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    /**
     * Dashboard istatistikleri
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(reportingService.getDashboardStats());
    }

    /**
     * Aylık gelir raporu
     */
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<Map<String, Object>> getMonthlyReport(
            @PathVariable int year, 
            @PathVariable int month) {
        
        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Geçersiz ay değeri. 1-12 arası olmalıdır.")
            );
        }
        
        return ResponseEntity.ok(reportingService.getMonthlyRevenueReport(year, month));
    }

    /**
     * Yıllık rapor
     */
    @GetMapping("/yearly/{year}")
    public ResponseEntity<Map<String, Object>> getYearlyReport(@PathVariable int year) {
        int currentYear = LocalDate.now().getYear();
        
        if (year > currentYear + 1 || year < 2020) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Geçersiz yıl değeri.")
            );
        }
        
        return ResponseEntity.ok(reportingService.getYearlyReport(year));
    }

    /**
     * Müşteri analiz raporu
     */
    @GetMapping("/customers")
    public ResponseEntity<Map<String, Object>> getCustomerAnalytics() {
        return ResponseEntity.ok(reportingService.getCustomerAnalyticsReport());
    }

    /**
     * Sipariş durum raporu
     */
    @GetMapping("/orders/status")
    public ResponseEntity<Map<String, Object>> getOrderStatusReport() {
        return ResponseEntity.ok(reportingService.getOrderStatusReport());
    }

    /**
     * Mevcut ay raporu (kolaylık için)
     */
    @GetMapping("/current-month")
    public ResponseEntity<Map<String, Object>> getCurrentMonthReport() {
        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(
            reportingService.getMonthlyRevenueReport(now.getYear(), now.getMonthValue())
        );
    }

    /**
     * Mevcut yıl raporu (kolaylık için)
     */
    @GetMapping("/current-year")
    public ResponseEntity<Map<String, Object>> getCurrentYearReport() {
        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(reportingService.getYearlyReport(now.getYear()));
    }

    /**
     * Tarih aralığı bazlı özel rapor
     */
    @GetMapping("/custom")
    public ResponseEntity<Map<String, Object>> getCustomReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            if (start.isAfter(end)) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Başlangıç tarihi bitiş tarihinden sonra olamaz.")
                );
            }
            
            // Basit custom rapor - geliştirilecek
            Map<String, Object> customReport = Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "message", "Özel rapor özelliği yakında eklenecek"
            );
            
            return ResponseEntity.ok(customReport);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Geçersiz tarih formatı. YYYY-MM-DD kullanın.")
            );
        }
    }
} 