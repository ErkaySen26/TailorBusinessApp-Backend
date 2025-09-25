package erdalguda.main.service;

import erdalguda.main.repository.OrderRepository;
import erdalguda.main.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportingService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    
    @Autowired
    public ReportingService(OrderRepository orderRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Aylık gelir raporu
     */
    public Map<String, Object> getMonthlyRevenueReport(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        var orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        
        Double totalRevenue = orders.stream()
            .filter(order -> order.getTotalPrice() != null)
            .mapToDouble(order -> order.getTotalPrice())
            .sum();
            
        Long totalOrders = (long) orders.size();
        
        // Ürün tipine göre gelir dağılımı
        Map<String, Double> revenueByProduct = orders.stream()
            .filter(order -> order.getTotalPrice() != null)
            .collect(Collectors.groupingBy(
                order -> order.getProductType().getDisplayName(),
                Collectors.summingDouble(order -> order.getTotalPrice())
            ));

        // Günlük gelir trendi
        Map<String, Double> dailyRevenue = orders.stream()
            .filter(order -> order.getTotalPrice() != null)
            .collect(Collectors.groupingBy(
                order -> order.getOrderDate().toString(),
                Collectors.summingDouble(order -> order.getTotalPrice())
            ));

        Map<String, Object> report = new HashMap<>();
        report.put("period", startDate.getMonth().name() + " " + year);
        report.put("totalRevenue", totalRevenue);
        report.put("totalOrders", totalOrders);
        report.put("averageOrderValue", totalOrders > 0 ? totalRevenue / totalOrders : 0);
        report.put("revenueByProduct", revenueByProduct);
        report.put("dailyRevenue", dailyRevenue);
        
        return report;
    }

    /**
     * Yıllık özet raporu
     */
    public Map<String, Object> getYearlyReport(int year) {
        List<Map<String, Object>> monthlyReports = new ArrayList<>();
        
        for (int month = 1; month <= 12; month++) {
            monthlyReports.add(getMonthlyRevenueReport(year, month));
        }
        
        Double yearlyRevenue = monthlyReports.stream()
            .mapToDouble(report -> (Double) report.get("totalRevenue"))
            .sum();
            
        Long yearlyOrders = monthlyReports.stream()
            .mapToLong(report -> (Long) report.get("totalOrders"))
            .sum();

        Map<String, Object> yearlyReport = new HashMap<>();
        yearlyReport.put("year", year);
        yearlyReport.put("totalRevenue", yearlyRevenue);
        yearlyReport.put("totalOrders", yearlyOrders);
        yearlyReport.put("averageOrderValue", yearlyOrders > 0 ? yearlyRevenue / yearlyOrders : 0);
        yearlyReport.put("monthlyBreakdown", monthlyReports);
        
        return yearlyReport;
    }

    /**
     * Müşteri analiz raporu
     */
    public Map<String, Object> getCustomerAnalyticsReport() {
        Long totalCustomers = customerRepository.count();
        
        // En çok sipariş veren müşteriler
        var topCustomers = orderRepository.findAll().stream()
            .collect(Collectors.groupingBy(
                order -> order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName(),
                Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));

        // En karlı müşteriler
        var topRevenueCustomers = orderRepository.findAll().stream()
            .filter(order -> order.getTotalPrice() != null)
            .collect(Collectors.groupingBy(
                order -> order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName(),
                Collectors.summingDouble(order -> order.getTotalPrice())
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));

        Map<String, Object> report = new HashMap<>();
        report.put("totalCustomers", totalCustomers);
        report.put("topCustomersByOrders", topCustomers);
        report.put("topCustomersByRevenue", topRevenueCustomers);
        
        return report;
    }

    /**
     * Durum bazlı sipariş analizi
     */
    public Map<String, Object> getOrderStatusReport() {
        var allOrders = orderRepository.findAll();
        
        Map<String, Long> statusDistribution = allOrders.stream()
            .collect(Collectors.groupingBy(
                order -> order.getStatus().toString(),
                Collectors.counting()
            ));

        // Ortalama tamamlanma süreleri
        var completedOrders = allOrders.stream()
            .filter(order -> order.getDeliveryDate() != null)
            .collect(Collectors.toList());

        OptionalDouble avgCompletionDays = completedOrders.stream()
            .mapToLong(order -> java.time.temporal.ChronoUnit.DAYS.between(
                order.getOrderDate(), order.getDeliveryDate()))
            .average();

        Map<String, Object> report = new HashMap<>();
        report.put("statusDistribution", statusDistribution);
        report.put("totalOrders", (long) allOrders.size());
        report.put("completedOrders", (long) completedOrders.size());
        report.put("averageCompletionDays", avgCompletionDays.orElse(0.0));
        
        return report;
    }

    /**
     * Dashboard için özet istatistikler
     */
    public Map<String, Object> getDashboardStats() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        
        Map<String, Object> currentMonth = getMonthlyRevenueReport(now.getYear(), now.getMonthValue());
        Map<String, Object> statusReport = getOrderStatusReport();
        Long totalCustomers = customerRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("monthlyRevenue", currentMonth.get("totalRevenue"));
        stats.put("monthlyOrders", currentMonth.get("totalOrders"));
        stats.put("totalCustomers", totalCustomers);
        stats.put("orderStatusDistribution", statusReport.get("statusDistribution"));
        stats.put("averageOrderValue", currentMonth.get("averageOrderValue"));
        
        return stats;
    }
} 