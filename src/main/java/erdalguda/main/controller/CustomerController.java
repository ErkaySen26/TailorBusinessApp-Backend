package erdalguda.main.controller;

import erdalguda.main.dto.CustomerResponse;
import erdalguda.main.model.Customer;
import erdalguda.main.repository.CustomerRepository;
import erdalguda.main.repository.OrderRepository;
import erdalguda.main.service.EmailService;
import erdalguda.main.service.CacheEvictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepo;
    private final OrderRepository orderRepo;
    private final EmailService emailService;
    private final CacheEvictionService cacheEvictionService;

    @Autowired
    public CustomerController(CustomerRepository customerRepo, OrderRepository orderRepo, EmailService emailService,
                            CacheEvictionService cacheEvictionService) {
        this.customerRepo = customerRepo;
        this.orderRepo = orderRepo;
        this.emailService = emailService;
        this.cacheEvictionService = cacheEvictionService;
    }

    @GetMapping
    public List<CustomerResponse> getAll() {
        return customerRepo.findAll().stream()
                .map(CustomerResponse::new)
                .toList();
    }

    @PostMapping
    @Transactional
    public Customer create(@RequestBody Customer customer) {
        Customer saved = customerRepo.save(customer);
        
        // Cache'leri temizle - yeni müşteri eklendi
        cacheEvictionService.evictOnNewCustomer();
        
        // Email gönderimini async yapalım
        if (saved.getEmail() != null && !saved.getEmail().trim().isEmpty()) {
            emailService.sendWelcomeEmail(saved.getEmail(), saved.getFirstName(), saved.getLastName());
        }
        
        return saved;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        return customerRepo.findById(id)
                .map(CustomerResponse::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<CustomerResponse> searchByName(@RequestParam String firstName) {
        return customerRepo.findByFirstNameContainingIgnoreCase(firstName)
                .stream()
                .map(CustomerResponse::new)
                .toList();
    }

    @GetMapping("/advanced-search")
    public List<CustomerResponse> advancedSearch(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email) {

        List<Customer> result = customerRepo.findByAdvancedSearch(firstName, lastName, phone, email);

        return result.stream()
                .map(CustomerResponse::new)
                .toList();
    }

    @GetMapping("/page")
    public Page<Customer> getPagedCustomers(Pageable pageable) {
        return customerRepo.findAll(pageable);
    }

    @GetMapping("/search-paginated")
    public Page<Customer> searchCustomersPaginated(
            @RequestParam String searchTerm, 
            Pageable pageable) {
        return customerRepo.findByFullNameContaining(searchTerm, pageable);
    }

    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<Customer> partialUpdate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        Optional<Customer> optionalCustomer = customerRepo.findById(id);
        if (optionalCustomer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Customer customer = optionalCustomer.get();

        // Güvenli update işlemi
        if (updates.containsKey("firstName")) {
            customer.setFirstName((String) updates.get("firstName"));
        }
        if (updates.containsKey("lastName")) {
            customer.setLastName((String) updates.get("lastName"));
        }
        if (updates.containsKey("address")) {
            customer.setAddress((String) updates.get("address"));
        }
        if (updates.containsKey("phone")) {
            customer.setPhone((String) updates.get("phone"));
        }
        if (updates.containsKey("email")) {
            customer.setEmail((String) updates.get("email"));
        }
        if (updates.containsKey("height")) {
            customer.setHeight(Double.valueOf(updates.get("height").toString()));
        }
        if (updates.containsKey("weight")) {
            customer.setWeight(Double.valueOf(updates.get("weight").toString()));
        }

        return ResponseEntity.ok(customerRepo.save(customer));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer updated) {
        return customerRepo.findById(id)
                .map(customer -> {
                    customer.setFirstName(updated.getFirstName());
                    customer.setLastName(updated.getLastName());
                    customer.setAddress(updated.getAddress());
                    customer.setPhone(updated.getPhone());
                    customer.setEmail(updated.getEmail());
                    customer.setHeight(updated.getHeight());
                    customer.setWeight(updated.getWeight());
                    return ResponseEntity.ok(customerRepo.save(customer));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countCustomers() {
        long count = customerRepo.count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCustomerStatistics() {
        long totalCustomers = customerRepo.getTotalCustomerCount();
        long customersWithEmail = customerRepo.getCustomersWithEmailCount();
        
        return ResponseEntity.ok(Map.of(
            "totalCustomers", totalCustomers,
            "customersWithEmail", customersWithEmail,
            "emailPercentage", totalCustomers > 0 ? (customersWithEmail * 100.0 / totalCustomers) : 0
        ));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Customer>> getRecentCustomers(@RequestParam(defaultValue = "10") int limit) {
        List<Customer> recentCustomers = customerRepo.findRecentCustomers(limit);
        return ResponseEntity.ok(recentCustomers);
    }

    @GetMapping("/without-orders")
    public ResponseEntity<List<CustomerResponse>> getCustomersWithoutOrders() {
        return ResponseEntity.ok(
            customerRepo.findCustomersWithoutOrders()
                .stream()
                .map(CustomerResponse::new)
                .toList()
        );
    }

    @GetMapping("/high-value")
    public ResponseEntity<List<Customer>> getHighValueCustomers(
            @RequestParam(defaultValue = "1000") Double minSpent) {
        return ResponseEntity.ok(customerRepo.findHighValueCustomers(minSpent));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!customerRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // Önce ilgili siparişleri sil
        orderRepo.deleteByCustomerId(id);
        // Sonra müşteriyi sil
        customerRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/safe/{id}")
    @Transactional
    public ResponseEntity<?> safeDelete(@PathVariable Long id) {
        try {
            if (!customerRepo.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            // Transaction içinde tüm ilişkili verileri temizle
            orderRepo.deleteByCustomerId(id);
            customerRepo.deleteById(id);
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Cannot delete customer. Error: " + e.getMessage());
        }
    }
}
