package erdalguda.main.controller;

import erdalguda.main.dto.DashboardStatsDto;
import erdalguda.main.dto.OrderStatusDistributionDto;
import erdalguda.main.dto.MonthlyTrendDto;
import erdalguda.main.dto.ProductDistributionDto;
import erdalguda.main.repository.CustomerRepository;
import erdalguda.main.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final CustomerRepository customerRepo;
    private final OrderRepository orderRepo;

    @Autowired
    public DashboardController(CustomerRepository customerRepo, OrderRepository orderRepo) {
        this.customerRepo = customerRepo;
        this.orderRepo = orderRepo;
    }

    @GetMapping("/stats")
    @Cacheable(value = "dashboard-stats", key = "'daily-stats-' + T(java.time.LocalDate).now().toString()")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate thirtyDaysAgo = today.minusDays(30);
            LocalDate oneMonthAgo = today.minusMonths(1);
            LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
            LocalDate endOfWeek = startOfWeek.plusDays(6);
            
            // Mevcut ayın başlangıç ve bitiş tarihleri
            LocalDate startOfCurrentMonth = today.withDayOfMonth(1);
            LocalDate endOfCurrentMonth = today.with(TemporalAdjusters.lastDayOfMonth());
            
            // 1. Toplam müşteri sayısı
            Long totalCustomers = customerRepo.count();
            
            // 2. Son 30 günde eklenen sipariş sayısı
            Long ordersLast30Days = orderRepo.countOrdersByDateRange(thirtyDaysAgo, today);
            
            // 3. BU AYDA teslim edilen siparişlerin toplam cirosu
            Double revenueThisMonth = orderRepo.sumRevenueByDateRange(startOfCurrentMonth, endOfCurrentMonth);
            if (revenueThisMonth == null) revenueThisMonth = 0.0;
            
            // 4. Prova bekleyen sipariş sayısı
            Long fittingOrders = orderRepo.countByStatus(erdalguda.main.model.Order.OrderStatus.FITTING);
            
            // 5. Son 1 ayda tamamlanan sipariş sayısı
            Long completedLastMonth = orderRepo.countCompletedOrdersByDateRange(oneMonthAgo, today);
            
            // 6. Bu hafta teslim edilecek sipariş sayısı
            Long deliveriesThisWeek = orderRepo.countDeliveriesForWeek(startOfWeek, endOfWeek);
            
            // Büyüme hesaplamaları
            LocalDate sixtyDaysAgo = today.minusDays(60);
            Long ordersPrevious30Days = orderRepo.countOrdersByDateRange(sixtyDaysAgo, thirtyDaysAgo);
            Double ordersGrowth = 0.0;
            if (ordersPrevious30Days > 0) {
                ordersGrowth = ((ordersLast30Days - ordersPrevious30Days) * 100.0) / ordersPrevious30Days;
            }
            
            // Geçen ayın cirosu ile karşılaştırma
            LocalDate startOfLastMonth = startOfCurrentMonth.minusMonths(1);
            LocalDate endOfLastMonth = startOfCurrentMonth.minusDays(1);
            Double revenueLastMonth = orderRepo.sumRevenueByDateRange(startOfLastMonth, endOfLastMonth);
            if (revenueLastMonth == null) revenueLastMonth = 0.0;
            
            Double revenueGrowth = 0.0;
            if (revenueLastMonth > 0) {
                revenueGrowth = ((revenueThisMonth - revenueLastMonth) * 100.0) / revenueLastMonth;
            }
            
            DashboardStatsDto stats = new DashboardStatsDto();
            stats.setTotalCustomers(totalCustomers);
            stats.setOrdersLast30Days(ordersLast30Days);
            stats.setRevenueLastMonth(revenueThisMonth); // Bu ayın cirosu
            stats.setFittingOrders(fittingOrders);
            stats.setCompletedLastMonth(completedLastMonth);
            stats.setDeliveriesThisWeek(deliveriesThisWeek);
            stats.setOrdersGrowth(ordersGrowth);
            stats.setRevenueGrowth(revenueGrowth);
            stats.setCustomersGrowth(8.5); // Müşteri growth için şimdilik varsayılan
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/order-status-distribution")
    @Cacheable(value = "order-status-distribution", key = "'status-dist-' + T(java.time.LocalDate).now().toString()")
    public ResponseEntity<List<OrderStatusDistributionDto>> getOrderStatusDistribution() {
        try {
            List<Object[]> results = orderRepo.getOrderStatusDistribution();
            List<OrderStatusDistributionDto> distribution = results.stream()
                .map(result -> new OrderStatusDistributionDto(
                    result[0].toString(), 
                    ((Number) result[1]).longValue()
                ))
                .toList();
            return ResponseEntity.ok(distribution);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/monthly-trend")
    @Cacheable(value = "monthly-trend", key = "'trend-' + T(java.time.LocalDate).now().toString()")
    public ResponseEntity<List<MonthlyTrendDto>> getMonthlyTrend() {
        try {
            LocalDate eightMonthsAgo = LocalDate.now().minusMonths(8);
            List<Object[]> results = orderRepo.getMonthlyTrend(eightMonthsAgo);
            List<MonthlyTrendDto> trend = results.stream()
                .map(result -> new MonthlyTrendDto(
                    ((Number) result[0]).intValue(), // year
                    ((Number) result[1]).intValue(), // month
                    ((Number) result[2]).longValue(), // order count
                    result[3] != null ? ((Number) result[3]).doubleValue() : 0.0 // revenue
                ))
                .toList();
            return ResponseEntity.ok(trend);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/product-distribution")
    @Cacheable(value = "product-distribution", key = "'product-dist-' + T(java.time.LocalDate).now().toString()")
    public ResponseEntity<List<ProductDistributionDto>> getProductDistribution() {
        try {
            List<Object[]> results = orderRepo.getProductTypeDistribution();
            List<ProductDistributionDto> distribution = results.stream()
                .map(result -> new ProductDistributionDto(
                    result[0] != null ? result[0].toString() : "Diğer",
                    ((Number) result[1]).longValue()
                ))
                .toList();
            return ResponseEntity.ok(distribution);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/recent-orders")
    @Cacheable(value = "recent-orders", key = "'recent-' + T(java.time.LocalDate).now().toString() + '-' + T(java.time.LocalTime).now().getHour()")
    public ResponseEntity<List<Map<String, Object>>> getRecentOrders() {
        try {
            List<Object[]> results = orderRepo.getRecentOrders();
            List<Map<String, Object>> recentOrders = results.stream()
                .map(result -> {
                    String firstName = result[1] != null ? result[1].toString() : "";
                    String lastName = result[2] != null ? result[2].toString() : "";
                    String customerName = (firstName + " " + lastName).trim();
                    
                    return Map.of(
                        "id", result[0],
                        "customerName", customerName.isEmpty() ? "Bilinmeyen Müşteri" : customerName,
                        "productType", result[3] != null ? result[3] : "Belirtilmemiş",
                        "status", result[4] != null ? result[4] : "UNKNOWN",
                        "totalPrice", result[5] != null ? result[5] : 0,
                        "orderDate", result[6] != null ? result[6] : ""
                    );
                })
                .toList();
            return ResponseEntity.ok(recentOrders);
        } catch (Exception e) {
            e.printStackTrace(); // Debug için hata yazdırıyoruz
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== CACHE MANAGEMENT ENDPOINTS =====

    @PostMapping("/cache/clear-all")
    @CacheEvict(value = {"dashboard-stats", "order-status-distribution", "monthly-trend", "product-distribution", "recent-orders"}, allEntries = true)
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        return ResponseEntity.ok(Map.of("message", "Tüm cache'ler temizlendi"));
    }

    @PostMapping("/cache/clear/{cacheName}")
    @CacheEvict(value = "#cacheName", allEntries = true)
    public ResponseEntity<Map<String, String>> clearSpecificCache(@PathVariable String cacheName) {
        return ResponseEntity.ok(Map.of("message", cacheName + " cache'i temizlendi"));
    }

    // ===== ADDITIONAL PERFORMANCE ENDPOINTS =====

    @GetMapping("/performance/cache-status")
    public ResponseEntity<Map<String, Object>> getCacheStatus() {
        // Cache durumu hakkında bilgi verme
        return ResponseEntity.ok(Map.of(
            "cacheEnabled", true,
            "activeCaches", List.of("dashboard-stats", "order-status-distribution", "monthly-trend", "product-distribution", "recent-orders"),
            "lastCacheUpdate", LocalDateTime.now().toString()
        ));
    }
} 