package erdalguda.main.repository;

import erdalguda.main.model.Order;
import erdalguda.main.model.Order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // ===== OPTIMIZED FETCH METHODS =====
    
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.customer")
    List<Order> findAllWithCustomers();

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.customer WHERE o.id = :id")
    Optional<Order> findByIdWithCustomer(@Param("id") Long id);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.customer WHERE o.customer.id = :customerId ORDER BY o.createdAt DESC")
    List<Order> findByCustomerIdWithCustomer(@Param("customerId") Long customerId);

    // ===== BASIC QUERY METHODS =====

    List<Order> findByCustomerId(Long customerId);

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.orderDate ASC")
    List<Order> findByStatus(@Param("status") OrderStatus status);

    @Modifying
    @Transactional
    @Query("DELETE FROM Order o WHERE o.customer.id = :customerId")
    void deleteByCustomerId(@Param("customerId") Long customerId);

    // ===== DATE RANGE QUERIES WITH OPTIMIZATION =====
    
    @Query("SELECT o FROM Order o WHERE o.orderDate >= :startDate AND o.orderDate <= :endDate ORDER BY o.orderDate DESC")
    List<Order> findByOrderDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.customer WHERE " +
           "o.orderDate >= :startDate AND o.orderDate <= :endDate " +
           "ORDER BY o.orderDate DESC")
    List<Order> findByOrderDateBetweenWithCustomer(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // ===== ACTIVE ORDERS OPTIMIZATION =====
    
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.customer WHERE " +
           "o.customer.id = :customerId AND o.status != 'CANCELLED' AND o.status != 'DELIVERED' " +
           "ORDER BY o.createdAt DESC")
    List<Order> findActiveOrdersByCustomer(@Param("customerId") Long customerId);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.customer WHERE " +
           "o.status IN ('PREPARING', 'CUTTING', 'SEWING', 'FITTING') " +
           "ORDER BY o.orderDate ASC")
    List<Order> findInProgressOrders();

    // ===== STATUS-BASED QUERIES =====
    
    @Query("SELECT o FROM Order o WHERE " +
           "o.estimatedDeliveryDate < :date AND o.status != :excludeStatus " +
           "ORDER BY o.estimatedDeliveryDate ASC")
    List<Order> findByEstimatedDeliveryDateBeforeAndStatusNot(
            @Param("date") LocalDate date, 
            @Param("excludeStatus") OrderStatus excludeStatus);

    // ===== PAGINATED QUERIES =====
    
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.customer")
    Page<Order> findAllWithCustomers(Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.customer WHERE o.status = :status")
    Page<Order> findByStatusWithCustomer(@Param("status") OrderStatus status, Pageable pageable);

    // ===== COUNT QUERIES FOR PERFORMANCE =====

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startDate AND o.orderDate <= :endDate")
    Long countOrdersByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE " +
           "o.orderDate >= :startDate AND o.orderDate <= :endDate AND o.status = 'DELIVERED'")
    Long countCompletedOrdersByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE " +
           "o.estimatedDeliveryDate >= :startDate AND o.estimatedDeliveryDate <= :endDate " +
           "AND o.status NOT IN ('DELIVERED', 'CANCELLED')")
    Long countDeliveriesForWeek(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // ===== REVENUE CALCULATIONS =====
    
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE " +
           "o.orderDate >= :startDate AND o.orderDate <= :endDate AND o.status = 'DELIVERED'")
    Double sumRevenueByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(AVG(o.totalPrice), 0) FROM Order o WHERE " +
           "o.orderDate >= :startDate AND o.orderDate <= :endDate AND o.status = 'DELIVERED'")
    Double averageRevenueByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // ===== STATISTICS QUERIES =====
    
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status ORDER BY COUNT(o) DESC")
    List<Object[]> getOrderStatusDistribution();

    @Query("SELECT o.productType, COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate GROUP BY o.productType ORDER BY COUNT(o) DESC")
    List<Object[]> getProductTypeStatistics(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT c.firstName, c.lastName, SUM(o.totalPrice) as totalSpent, COUNT(o) as orderCount " +
           "FROM Order o JOIN o.customer c WHERE o.status = 'DELIVERED' " +
           "GROUP BY c.id, c.firstName, c.lastName " +
           "HAVING COUNT(o) >= :minOrders " +
           "ORDER BY totalSpent DESC")
    List<Object[]> getTopCustomersBySpending(@Param("minOrders") int minOrders);

    // ===== PERFORMANCE MONITORING =====
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :date")
    Long countOrdersCreatedSince(@Param("date") java.time.LocalDateTime date);

    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.orderDate >= :date GROUP BY o.status")
    List<Object[]> getStatusDistributionSince(@Param("date") LocalDate date);

    // ===== MISSING METHODS =====
    
    @Query("SELECT o.productType, COUNT(o) FROM Order o GROUP BY o.productType ORDER BY COUNT(o) DESC")
    List<Object[]> getProductTypeDistribution();

    @Query(value = "SELECT EXTRACT(YEAR FROM order_date) as year, " +
                   "EXTRACT(MONTH FROM order_date) as month, " +
                   "COUNT(*) as order_count, " +
                   "COALESCE(SUM(CASE WHEN status = 'DELIVERED' THEN total_price ELSE 0 END), 0) as revenue " +
                   "FROM orders WHERE order_date >= :startDate " +
                   "GROUP BY EXTRACT(YEAR FROM order_date), EXTRACT(MONTH FROM order_date) " +
                   "ORDER BY year DESC, month DESC", nativeQuery = true)
    List<Object[]> getMonthlyTrend(@Param("startDate") LocalDate startDate);

    @Query(value = "SELECT o.id, c.first_name, c.last_name, o.product_type, o.status, " +
                   "o.total_price, o.order_date " +
                   "FROM orders o " +
                   "INNER JOIN customer c ON o.customer_id = c.id " +
                   "ORDER BY o.order_date DESC " +
                   "LIMIT 5", nativeQuery = true)
    List<Object[]> getRecentOrders();

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.customer WHERE " +
           "o.estimatedDeliveryDate BETWEEN :startDate AND :endDate " +
           "AND o.status NOT IN ('DELIVERED', 'CANCELLED') " +
           "ORDER BY o.estimatedDeliveryDate ASC")
    List<Order> findUpcomingDeliveries(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.customer WHERE " +
           "o.estimatedDeliveryDate < :currentDate " +
           "AND o.status NOT IN ('DELIVERED', 'CANCELLED') " +
           "ORDER BY o.estimatedDeliveryDate ASC")
    List<Order> findOverdueOrders(@Param("currentDate") LocalDate currentDate);
}