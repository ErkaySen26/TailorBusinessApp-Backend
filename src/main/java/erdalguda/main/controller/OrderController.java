package erdalguda.main.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import erdalguda.main.dto.OrderRequest;
import erdalguda.main.dto.OrderResponse;
import erdalguda.main.dto.UpdateOrderRequest;
import erdalguda.main.model.Order;
import erdalguda.main.model.Order.OrderStatus;
import erdalguda.main.model.Customer;
import erdalguda.main.repository.OrderRepository;
import erdalguda.main.repository.CustomerRepository;
import erdalguda.main.service.EmailService;
import erdalguda.main.service.CacheEvictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderRepository orderRepo;
    private final CustomerRepository customerRepo;
    private final EmailService emailService;
    private final CacheEvictionService cacheEvictionService;

    @Autowired
    public OrderController(OrderRepository orderRepo, CustomerRepository customerRepo, 
                          EmailService emailService, CacheEvictionService cacheEvictionService) {
        this.orderRepo = orderRepo;
        this.customerRepo = customerRepo;
        this.emailService = emailService;
        this.cacheEvictionService = cacheEvictionService;
    }

    // === SİPARİŞ CRUD İŞLEMLERİ ===

    @PostMapping("/new")
    @Transactional
    public ResponseEntity<?> createNewOrder(@RequestBody OrderRequest orderRequest) {
        logger.info("🆕 Yeni sipariş oluşturma isteği alındı");
        logger.info("📝 OrderRequest: {}", orderRequest);
        logger.info("📋 Notes içeriği: {}", orderRequest.getNotes());
        
        try {
            // Müşteri kontrolü
            Optional<Customer> customerOpt = customerRepo.findById(orderRequest.getCustomerId());
            if (customerOpt.isEmpty()) {
                logger.warn("❌ Müşteri bulunamadı: {}", orderRequest.getCustomerId());
                return ResponseEntity.badRequest().body("Müşteri bulunamadı");
            }

            Customer customer = customerOpt.get();
            Order order = new Order();
            
            // Temel sipariş bilgileri
            order.setCustomer(customer);
            order.setProductType(Order.ProductType.valueOf(orderRequest.getProductType()));
            order.setStatus(Order.OrderStatus.valueOf(orderRequest.getStatus()));
            order.setNotes(orderRequest.getNotes());
            order.setTotalPrice(orderRequest.getTotalPrice());
            order.setOrderDate(LocalDate.now());
            order.setCreatedAt(LocalDateTime.now());

            // Estimated delivery date'i parse et
            if (orderRequest.getEstimatedDeliveryDate() != null && !orderRequest.getEstimatedDeliveryDate().isEmpty()) {
                order.setEstimatedDeliveryDate(LocalDate.parse(orderRequest.getEstimatedDeliveryDate()));
            }

            // === ÜRÜN ÖZELLEŞTİRMELERİ ===
            // Gömlek özelleştirmeleri
            if (orderRequest.getCollarType() != null && !orderRequest.getCollarType().isEmpty()) {
                order.setCollarType(Order.CollarType.valueOf(orderRequest.getCollarType()));
            }
            if (orderRequest.getSleeveType() != null && !orderRequest.getSleeveType().isEmpty()) {
                order.setSleeveType(Order.SleeveType.valueOf(orderRequest.getSleeveType()));
            }

            // Pantolon özelleştirmeleri
            if (orderRequest.getWaistType() != null && !orderRequest.getWaistType().isEmpty()) {
                order.setWaistType(Order.WaistType.valueOf(orderRequest.getWaistType()));
            }
            if (orderRequest.getPleatType() != null && !orderRequest.getPleatType().isEmpty()) {
                order.setPleatType(Order.PleatType.valueOf(orderRequest.getPleatType()));
            }
            if (orderRequest.getLegType() != null && !orderRequest.getLegType().isEmpty()) {
                order.setLegType(Order.LegType.valueOf(orderRequest.getLegType()));
            }

            // Ceket özelleştirmeleri
            if (orderRequest.getButtonType() != null && !orderRequest.getButtonType().isEmpty()) {
                order.setButtonType(Order.ButtonType.valueOf(orderRequest.getButtonType()));
            }
            if (orderRequest.getPocketType() != null && !orderRequest.getPocketType().isEmpty()) {
                order.setPocketType(Order.PocketType.valueOf(orderRequest.getPocketType()));
            }
            if (orderRequest.getVentType() != null && !orderRequest.getVentType().isEmpty()) {
                order.setVentType(Order.VentType.valueOf(orderRequest.getVentType()));
            }
            if (orderRequest.getBackType() != null && !orderRequest.getBackType().isEmpty()) {
                order.setBackType(Order.BackType.valueOf(orderRequest.getBackType()));
            }

            Order savedOrder = orderRepo.save(order);
            
            // Cache'leri temizle ve email gönder
            cacheEvictionService.evictOnOrderStatusChange(); // mevcut metod kullan
            // emailService.sendNewOrderConfirmationEmail(savedOrder); // bu metod yok, kaldır
            
            return ResponseEntity.ok(savedOrder);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Geçersiz enum değeri: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Sipariş oluşturulurken hata oluştu: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/advanced")
    @Transactional
    public ResponseEntity<?> updateOrderAdvanced(@PathVariable Long id, @RequestBody UpdateOrderRequest updateRequest) {
        try {
            Optional<Order> orderOpt = orderRepo.findById(id);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Order order = orderOpt.get();
            Order.OrderStatus oldStatus = order.getStatus();

            // Temel bilgiler güncelleme
            if (updateRequest.getStatus() != null && !updateRequest.getStatus().isEmpty()) {
                order.setStatus(Order.OrderStatus.valueOf(updateRequest.getStatus()));
            }
            if (updateRequest.getNotes() != null) {
                order.setNotes(updateRequest.getNotes());
            }
            if (updateRequest.getTotalPrice() != null) {
                order.setTotalPrice(updateRequest.getTotalPrice());
            }

            // Tarih güncellemeleri
            if (updateRequest.getEstimatedDeliveryDate() != null && !updateRequest.getEstimatedDeliveryDate().isEmpty()) {
                order.setEstimatedDeliveryDate(LocalDate.parse(updateRequest.getEstimatedDeliveryDate()));
            }
            if (updateRequest.getDeliveryDate() != null && !updateRequest.getDeliveryDate().isEmpty()) {
                order.setDeliveryDate(LocalDate.parse(updateRequest.getDeliveryDate()));
            }

            // === ÜRÜN ÖZELLEŞTİRMELERİ GÜNCELLEMELERİ ===
            // Gömlek özelleştirmeleri
            if (updateRequest.getCollarType() != null && !updateRequest.getCollarType().isEmpty()) {
                order.setCollarType(Order.CollarType.valueOf(updateRequest.getCollarType()));
            }
            if (updateRequest.getSleeveType() != null && !updateRequest.getSleeveType().isEmpty()) {
                order.setSleeveType(Order.SleeveType.valueOf(updateRequest.getSleeveType()));
            }

            // Pantolon özelleştirmeleri
            if (updateRequest.getWaistType() != null && !updateRequest.getWaistType().isEmpty()) {
                order.setWaistType(Order.WaistType.valueOf(updateRequest.getWaistType()));
            }
            if (updateRequest.getPleatType() != null && !updateRequest.getPleatType().isEmpty()) {
                order.setPleatType(Order.PleatType.valueOf(updateRequest.getPleatType()));
            }
            if (updateRequest.getLegType() != null && !updateRequest.getLegType().isEmpty()) {
                order.setLegType(Order.LegType.valueOf(updateRequest.getLegType()));
            }

            // Ceket özelleştirmeleri
            if (updateRequest.getButtonType() != null && !updateRequest.getButtonType().isEmpty()) {
                order.setButtonType(Order.ButtonType.valueOf(updateRequest.getButtonType()));
            }
            if (updateRequest.getPocketType() != null && !updateRequest.getPocketType().isEmpty()) {
                order.setPocketType(Order.PocketType.valueOf(updateRequest.getPocketType()));
            }
            if (updateRequest.getVentType() != null && !updateRequest.getVentType().isEmpty()) {
                order.setVentType(Order.VentType.valueOf(updateRequest.getVentType()));
            }
            if (updateRequest.getBackType() != null && !updateRequest.getBackType().isEmpty()) {
                order.setBackType(Order.BackType.valueOf(updateRequest.getBackType()));
            }

            Order savedOrder = orderRepo.save(order);
            
            // Durum değişikliği kontrolü
            if (oldStatus != savedOrder.getStatus()) {
                cacheEvictionService.evictOnOrderStatusChange();
                emailService.sendOrderStatusUpdateEmail(savedOrder);
            }
            
            return ResponseEntity.ok(savedOrder);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Geçersiz enum değeri: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Sipariş güncellenirken hata oluştu: " + e.getMessage());
        }
    }

    // === ESKİ ENDPOINT (GERİYE UYUMLULUK İÇİN) ===
    @PostMapping
    @Transactional
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        // Müşteri kontrolü
        if (order.getCustomer() == null || order.getCustomer().getId() == null) {
            return ResponseEntity.badRequest().body("Müşteri bilgisi gerekli");
        }

        Optional<Customer> customerOpt = customerRepo.findById(order.getCustomer().getId());
        if (customerOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Müşteri bulunamadı");
        }

        order.setCustomer(customerOpt.get());
        Order saved = orderRepo.save(order);
        
        // Cache'leri temizle - yeni sipariş eklendi
        cacheEvictionService.evictOnNewOrder();
        
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<Order> orders = orderRepo.findAllWithCustomers();
        List<OrderResponse> response = orders.stream()
                .map(OrderResponse::new)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<Order>> getAllOrdersPaginated(Pageable pageable) {
        Page<Order> orders = orderRepo.findAllWithCustomers(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        Optional<Order> orderOpt = orderRepo.findByIdWithCustomer(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new OrderResponse(orderOpt.get()));
    }

    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable Long customerId) {
        List<Order> orders = orderRepo.findByCustomerIdWithCustomer(customerId);
        List<OrderResponse> response = orders.stream()
                .map(OrderResponse::new)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active/by-customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getActiveOrdersByCustomer(@PathVariable Long customerId) {
        List<Order> orders = orderRepo.findActiveOrdersByCustomer(customerId);
        List<OrderResponse> response = orders.stream()
                .map(OrderResponse::new)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/in-progress")
    public ResponseEntity<List<OrderResponse>> getInProgressOrders() {
        List<Order> orders = orderRepo.findInProgressOrders();
        List<OrderResponse> response = orders.stream()
                .map(OrderResponse::new)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<Order> orders = orderRepo.findByStatus(status);
        List<OrderResponse> response = orders.stream()
                .map(OrderResponse::new)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-status/{status}/paginated")
    public ResponseEntity<Page<Order>> getOrdersByStatusPaginated(
            @PathVariable OrderStatus status, 
            Pageable pageable) {
        return ResponseEntity.ok(orderRepo.findByStatusWithCustomer(status, pageable));
    }

    @GetMapping("/by-date-range")
    public ResponseEntity<List<OrderResponse>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Order> orders = orderRepo.findByOrderDateBetweenWithCustomer(startDate, endDate);
        List<OrderResponse> response = orders.stream()
                .map(OrderResponse::new)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming-deliveries")
    public ResponseEntity<List<OrderResponse>> getUpcomingDeliveries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = LocalDate.now().plusDays(7);
        
        List<Order> orders = orderRepo.findUpcomingDeliveries(startDate, endDate);
        List<OrderResponse> response = orders.stream()
                .map(OrderResponse::new)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<OrderResponse>> getOverdueOrders() {
        List<Order> orders = orderRepo.findOverdueOrders(LocalDate.now());
        List<OrderResponse> response = orders.stream()
                .map(OrderResponse::new)
                .toList();
        return ResponseEntity.ok(response);
    }

    // === UTILITY ENDPOINTS - ENUM SEÇENEKLERİ ===
    @GetMapping("/options/product-types")
    public ResponseEntity<Map<String, Object>> getProductTypeOptions() {
        Map<String, Object> productTypes = new java.util.HashMap<>();
        for (Order.ProductType type : Order.ProductType.values()) {
            productTypes.put(type.name(), type.getDisplayName());
        }
        return ResponseEntity.ok(Map.of("productTypes", productTypes));
    }

    @GetMapping("/options/collar-types")
    public ResponseEntity<Map<String, Object>> getCollarTypeOptions() {
        Map<String, Object> collarTypes = new java.util.HashMap<>();
        for (Order.CollarType type : Order.CollarType.values()) {
            collarTypes.put(type.name(), type.getDisplayName());
        }
        return ResponseEntity.ok(Map.of("collarTypes", collarTypes));
    }

    @GetMapping("/options/sleeve-types")
    public ResponseEntity<Map<String, Object>> getSleeveTypeOptions() {
        Map<String, Object> sleeveTypes = new java.util.HashMap<>();
        for (Order.SleeveType type : Order.SleeveType.values()) {
            sleeveTypes.put(type.name(), type.getDisplayName());
        }
        return ResponseEntity.ok(Map.of("sleeveTypes", sleeveTypes));
    }

    @GetMapping("/options/waist-types")
    public ResponseEntity<Map<String, Object>> getWaistTypeOptions() {
        Map<String, Object> waistTypes = new java.util.HashMap<>();
        for (Order.WaistType type : Order.WaistType.values()) {
            waistTypes.put(type.name(), type.getDisplayName());
        }
        return ResponseEntity.ok(Map.of("waistTypes", waistTypes));
    }

    @GetMapping("/options/pleat-types")
    public ResponseEntity<Map<String, Object>> getPleatTypeOptions() {
        Map<String, Object> pleatTypes = new java.util.HashMap<>();
        for (Order.PleatType type : Order.PleatType.values()) {
            pleatTypes.put(type.name(), type.getDisplayName());
        }
        return ResponseEntity.ok(Map.of("pleatTypes", pleatTypes));
    }

    @GetMapping("/options/leg-types")
    public ResponseEntity<Map<String, Object>> getLegTypeOptions() {
        Map<String, Object> legTypes = new java.util.HashMap<>();
        for (Order.LegType type : Order.LegType.values()) {
            legTypes.put(type.name(), type.getDisplayName());
        }
        return ResponseEntity.ok(Map.of("legTypes", legTypes));
    }

    @GetMapping("/options/button-types")
    public ResponseEntity<Map<String, Object>> getButtonTypeOptions() {
        Map<String, Object> buttonTypes = new java.util.HashMap<>();
        for (Order.ButtonType type : Order.ButtonType.values()) {
            buttonTypes.put(type.name(), type.getDisplayName());
        }
        return ResponseEntity.ok(Map.of("buttonTypes", buttonTypes));
    }

    @GetMapping("/options/pocket-types")
    public ResponseEntity<Map<String, Object>> getPocketTypeOptions() {
        Map<String, Object> pocketTypes = new java.util.HashMap<>();
        for (Order.PocketType type : Order.PocketType.values()) {
            pocketTypes.put(type.name(), type.getDisplayName());
        }
        return ResponseEntity.ok(Map.of("pocketTypes", pocketTypes));
    }

    @GetMapping("/options/vent-types")
    public ResponseEntity<Map<String, Object>> getVentTypeOptions() {
        Map<String, Object> ventTypes = new java.util.HashMap<>();
        for (Order.VentType type : Order.VentType.values()) {
            ventTypes.put(type.name(), type.getDisplayName());
        }
        return ResponseEntity.ok(Map.of("ventTypes", ventTypes));
    }

    @GetMapping("/options/back-types")
    public ResponseEntity<Map<String, Object>> getBackTypeOptions() {
        Map<String, Object> backTypes = new java.util.HashMap<>();
        for (Order.BackType type : Order.BackType.values()) {
            backTypes.put(type.name(), type.getDisplayName());
        }
        return ResponseEntity.ok(Map.of("backTypes", backTypes));
    }

    @GetMapping("/options/all")
    public ResponseEntity<Map<String, Object>> getAllOrderOptions() {
        Map<String, Object> options = new java.util.HashMap<>();
        
        // Temel seçenekler
        Map<String, Object> productTypes = new java.util.HashMap<>();
        for (Order.ProductType type : Order.ProductType.values()) {
            productTypes.put(type.name(), type.getDisplayName());
        }
        options.put("productTypes", productTypes);

        Map<String, Object> statusTypes = new java.util.HashMap<>();
        for (OrderStatus type : OrderStatus.values()) {
            statusTypes.put(type.name(), type.getDisplayName());
        }
        options.put("statusTypes", statusTypes);

        // Gömlek seçenekleri
        Map<String, Object> collarTypes = new java.util.HashMap<>();
        for (Order.CollarType type : Order.CollarType.values()) {
            collarTypes.put(type.name(), type.getDisplayName());
    }
        options.put("collarTypes", collarTypes);

        Map<String, Object> sleeveTypes = new java.util.HashMap<>();
        for (Order.SleeveType type : Order.SleeveType.values()) {
            sleeveTypes.put(type.name(), type.getDisplayName());
        }
        options.put("sleeveTypes", sleeveTypes);

        // Pantolon seçenekleri
        Map<String, Object> waistTypes = new java.util.HashMap<>();
        for (Order.WaistType type : Order.WaistType.values()) {
            waistTypes.put(type.name(), type.getDisplayName());
        }
        options.put("waistTypes", waistTypes);

        Map<String, Object> pleatTypes = new java.util.HashMap<>();
        for (Order.PleatType type : Order.PleatType.values()) {
            pleatTypes.put(type.name(), type.getDisplayName());
        }
        options.put("pleatTypes", pleatTypes);

        Map<String, Object> legTypes = new java.util.HashMap<>();
        for (Order.LegType type : Order.LegType.values()) {
            legTypes.put(type.name(), type.getDisplayName());
        }
        options.put("legTypes", legTypes);

        // Ceket seçenekleri
        Map<String, Object> buttonTypes = new java.util.HashMap<>();
        for (Order.ButtonType type : Order.ButtonType.values()) {
            buttonTypes.put(type.name(), type.getDisplayName());
        }
        options.put("buttonTypes", buttonTypes);

        Map<String, Object> pocketTypes = new java.util.HashMap<>();
        for (Order.PocketType type : Order.PocketType.values()) {
            pocketTypes.put(type.name(), type.getDisplayName());
        }
        options.put("pocketTypes", pocketTypes);

        Map<String, Object> ventTypes = new java.util.HashMap<>();
        for (Order.VentType type : Order.VentType.values()) {
            ventTypes.put(type.name(), type.getDisplayName());
        }
        options.put("ventTypes", ventTypes);

        Map<String, Object> backTypes = new java.util.HashMap<>();
        for (Order.BackType type : Order.BackType.values()) {
            backTypes.put(type.name(), type.getDisplayName());
        }
        options.put("backTypes", backTypes);

        return ResponseEntity.ok(options);
    }

    // === İSTATİSTİK VE ANALİTİK ENDPOINT'LERİ ===
    @GetMapping("/statistics/by-product-type")
    public ResponseEntity<List<Object[]>> getProductTypeStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(orderRepo.getProductTypeStatistics(startDate, endDate));
    }

    @GetMapping("/statistics/top-customers")
    public ResponseEntity<List<Object[]>> getTopCustomers(
            @RequestParam(defaultValue = "1") int minOrders) {
        return ResponseEntity.ok(orderRepo.getTopCustomersBySpending(minOrders));
    }

    @GetMapping("/analytics/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Double totalRevenue = orderRepo.sumRevenueByDateRange(startDate, endDate);
        Double averageRevenue = orderRepo.averageRevenueByDateRange(startDate, endDate);
        Long orderCount = orderRepo.countCompletedOrdersByDateRange(startDate, endDate);
        
        return ResponseEntity.ok(Map.of(
            "totalRevenue", totalRevenue != null ? totalRevenue : 0.0,
            "averageRevenue", averageRevenue != null ? averageRevenue : 0.0,
            "orderCount", orderCount != null ? orderCount : 0L,
            "startDate", startDate,
            "endDate", endDate
        ));
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, OrderStatus> statusUpdate) {

        OrderStatus newStatus = statusUpdate.get("status");
        if (newStatus == null) {
            return ResponseEntity.badRequest().body("Yeni durum belirtilmedi");
        }

        Optional<Order> orderOpt = orderRepo.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Order order = orderOpt.get();
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        // Eğer durum DELIVERED ise, teslim tarihini güncelle
        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveryDate(LocalDate.now());
        }

        Order saved = orderRepo.save(order);
        
        // Durum değişikliği - cache'leri temizle ve email gönder
        if (oldStatus != newStatus) {
            cacheEvictionService.evictOnOrderStatusChange();
            emailService.sendOrderStatusUpdateEmail(saved);
        }
        
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        if (!orderRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        orderRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ===== PERFORMANCE MONITORING ENDPOINTS =====

    @GetMapping("/performance/count-since")
    public ResponseEntity<Long> getOrderCountSince(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime date) {
        return ResponseEntity.ok(orderRepo.countOrdersCreatedSince(date));
    }

    @GetMapping("/performance/status-distribution-since")
    public ResponseEntity<List<Object[]>> getStatusDistributionSince(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(orderRepo.getStatusDistributionSince(date));
    }
}