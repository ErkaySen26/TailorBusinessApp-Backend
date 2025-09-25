package erdalguda.main.repository;

import erdalguda.main.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // ===== OPTIMIZED FETCH METHODS =====
    


    // ===== SEARCH METHODS WITH PERFORMANCE OPTIMIZATION =====
    
    @Query("SELECT c FROM Customer c WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))")
    List<Customer> findByFirstNameContainingIgnoreCase(@Param("firstName") String firstName);

    @Query("SELECT c FROM Customer c WHERE LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<Customer> findByLastNameContainingIgnoreCase(@Param("lastName") String lastName);

    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) AND " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<Customer> findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
            @Param("firstName") String firstName, @Param("lastName") String lastName);

    @Query("SELECT c FROM Customer c WHERE " +
           "REPLACE(REPLACE(c.phone, ' ', ''), '-', '') LIKE CONCAT('%', REPLACE(REPLACE(:phone, ' ', ''), '-', ''), '%')")
    List<Customer> findByPhoneContaining(@Param("phone") String phone);

    // ===== ADVANCED SEARCH WITH INDEX HINTS =====
    
    @Query("SELECT c FROM Customer c WHERE " +
           "(:firstName IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:phone IS NULL OR REPLACE(REPLACE(c.phone, ' ', ''), '-', '') LIKE CONCAT('%', REPLACE(REPLACE(:phone, ' ', ''), '-', ''), '%')) AND " +
           "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    List<Customer> findByAdvancedSearch(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("phone") String phone,
            @Param("email") String email);

    // ===== PAGINATED SEARCH METHODS =====
    
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Customer> findByFullNameContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ===== BMI CALCULATIONS =====
    
    @Query("SELECT c FROM Customer c WHERE " +
            "c.height IS NOT NULL AND c.weight IS NOT NULL AND " +
            "(:minBmi IS NULL OR (c.weight / (c.height * c.height / 10000)) >= :minBmi) AND " +
            "(:maxBmi IS NULL OR (c.weight / (c.height * c.height / 10000)) <= :maxBmi)")
    List<Customer> findCustomersByBmiRange(
            @Param("minBmi") Double minBmi,
            @Param("maxBmi") Double maxBmi);

    // ===== ADDRESS SEARCH =====
    
    @Query("SELECT c FROM Customer c WHERE " +
           "c.address IS NOT NULL AND LOWER(c.address) LIKE LOWER(CONCAT('%', :addressKeyword, '%'))")
    List<Customer> findByAddressContainingIgnoreCase(@Param("addressKeyword") String addressKeyword);

    // ===== STATISTICS AND ANALYTICS =====
    
    @Query("SELECT COUNT(c) FROM Customer c")
    Long getTotalCustomerCount();

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.email IS NOT NULL AND c.email != ''")
    Long getCustomersWithEmailCount();



    // ===== RECENT CUSTOMERS =====
    
    @Query(value = "SELECT c.* FROM customer c ORDER BY c.id DESC LIMIT :limit", nativeQuery = true)
    List<Customer> findRecentCustomers(@Param("limit") int limit);

    // ===== CUSTOMERS WITHOUT ORDERS =====
    
    @Query("SELECT c FROM Customer c WHERE NOT EXISTS (SELECT 1 FROM Order o WHERE o.customer.id = c.id)")
    List<Customer> findCustomersWithoutOrders();

    // ===== HIGH VALUE CUSTOMERS =====
    
    @Query(value = "SELECT c.* FROM customer c " +
                   "LEFT JOIN orders o ON c.id = o.customer_id " +
                   "WHERE o.status = 'DELIVERED' " +
                   "GROUP BY c.id " +
                   "HAVING SUM(o.total_price) >= :minSpent " +
                   "ORDER BY SUM(o.total_price) DESC", nativeQuery = true)
    List<Customer> findHighValueCustomers(@Param("minSpent") Double minSpent);

    // ===== PERFORMANCE OPTIMIZED COUNT QUERIES =====
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Long countByNameSearch(@Param("searchTerm") String searchTerm);
}