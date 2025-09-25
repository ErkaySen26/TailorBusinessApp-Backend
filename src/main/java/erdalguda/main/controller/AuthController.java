package erdalguda.main.controller;

import erdalguda.main.model.Admin;
import erdalguda.main.model.Admin.Role;
import erdalguda.main.model.User;
import erdalguda.main.repository.AdminRepository;
import erdalguda.main.repository.UserRepository;
import erdalguda.main.service.AdminService;
import erdalguda.main.service.JwtService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AdminRepository adminRepo;
    
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private AdminService adminService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Uygulama başlangıcında varsayılan admin hesabı oluşturur
     * Bu metod aynı zamanda mevcut admin hesaplarının rollerini de kontrol eder ve düzeltir
     */
    @PostConstruct
    public void createDefaultAdmin() {
        // Önce veritabanında MANAGER rollerini USTA'ya çevir (SQL ile)
        try {
            logger.info("Veritabanı migration başlatılıyor...");
            int updatedRows = jdbcTemplate.update("UPDATE admin SET role = 'USTA' WHERE role = 'MANAGER'");
            if (updatedRows > 0) {
                logger.info("{} adet MANAGER rolü USTA'ya güncellendi", updatedRows);
            }
        } catch (Exception e) {
            logger.warn("Migration sırasında hata oluştu: {}", e.getMessage());
            // Hata durumunda tabloyu temizle
            try {
                jdbcTemplate.update("DELETE FROM admin WHERE role NOT IN ('ADMIN', 'USTA', 'MUHASEBECI')");
                logger.info("Geçersiz roller temizlendi");
            } catch (Exception cleanupEx) {
                logger.error("Tablo temizleme hatası: {}", cleanupEx.getMessage());
            }
        }
        
        // Varsayılan admin hesabı oluştur
        if (!adminRepo.existsById("erdalguda")) {
            Admin admin = new Admin();
            admin.setUsername("erdalguda");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN); // Ana admin kullanıcısı
            adminRepo.save(admin);
            logger.info("Varsayılan admin kullanıcısı oluşturuldu: {}", admin.getUsername());
        } else {
            // Eğer admin varsa, rolünün ADMIN olduğundan emin ol
            Optional<Admin> existingAdmin = adminRepo.findById("erdalguda");
            if (existingAdmin.isPresent()) {
                Admin admin = existingAdmin.get();
                if (admin.getRole() != Role.ADMIN) {
                    admin.setRole(Role.ADMIN);
                    adminRepo.save(admin);
                    logger.info("Varsayılan admin kullanıcısının rolü ADMIN olarak güncellendi");
                } else {
                    logger.info("Varsayılan admin kullanıcısı zaten ADMIN rolüne sahip");
                }
            }
        }
        
        // Eski "admin" kullanıcısını "erdalguda" olarak güncelle (migration)
        Optional<Admin> oldAdmin = adminRepo.findById("admin");
        if (oldAdmin.isPresent() && !adminRepo.existsById("erdalguda")) {
            Admin admin = oldAdmin.get();
            adminRepo.deleteById("admin"); // Eski admin'i sil
            admin.setUsername("erdalguda");
            admin.setPassword(passwordEncoder.encode("admin123"));
            adminRepo.save(admin);
            logger.info("Eski admin kullanıcısı erdalguda olarak güncellendi");
        }
        
        // Tüm kullanıcıları ve rollerini logla
        try {
            List<Admin> finalUsers = adminRepo.findAll();
            logger.info("Sistemdeki tüm kullanıcılar ve rolleri:");
            for (Admin user : finalUsers) {
                logger.info("Kullanıcı: {}, Rol: {}", user.getUsername(), user.getRole());
            }
        } catch (Exception e) {
            logger.error("Kullanıcı listeleme hatası: {}", e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        logger.info("Yeni kullanıcı kayıt denemesi: {}", request.getUsername());
        
        // Kullanıcı adı kontrolü (hem admin hem user tablolarında)
        if (adminRepo.existsById(request.getUsername()) || userRepo.existsByUsername(request.getUsername())) {
            logger.warn("Kullanıcı adı zaten kullanılıyor: {}", request.getUsername());
            return ResponseEntity.badRequest().body("Bu kullanıcı adı zaten kullanılıyor");
        }
        
        // E-posta kontrolü (eğer verilmişse)
        if (request.getEmail() != null && !request.getEmail().isEmpty() && userRepo.existsByEmail(request.getEmail())) {
            logger.warn("E-posta adresi zaten kullanılıyor: {}", request.getEmail());
            return ResponseEntity.badRequest().body("Bu e-posta adresi zaten kullanılıyor");
        }
        
        // Şifre uzunluk kontrolü
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body("Şifre en az 6 karakter olmalıdır");
        }
        
        try {
            // Rol kontrolü ve atama
            User.Role selectedRole = User.Role.MUHASEBECI; // Varsayılan rol
            if (request.getRole() != null && !request.getRole().isEmpty()) {
                try {
                    selectedRole = User.Role.valueOf(request.getRole().toUpperCase());
                    logger.info("Kullanıcı {} için {} rolü seçildi", request.getUsername(), selectedRole);
                } catch (IllegalArgumentException e) {
                    logger.warn("Geçersiz rol seçimi: {}, varsayılan MUHASEBECI atanacak", request.getRole());
                    selectedRole = User.Role.MUHASEBECI;
                }
            }
            
            // Yeni kullanıcı oluştur (onay bekliyor)
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setRole(selectedRole); // Seçilen rol
            user.setIsActive(false); // Onay bekliyor
            user.setIsApproved(false); // Admin onayı gerekli
            
            userRepo.save(user);
            logger.info("Yeni kullanıcı kaydedildi (onay bekliyor): {} - Rol: {}", user.getUsername(), user.getRole());
            
            return ResponseEntity.ok(Map.of(
                "message", "Kayıt başarılı! Hesabınız admin onayı bekliyor.",
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "status", "PENDING_APPROVAL"
            ));
            
        } catch (Exception e) {
            logger.error("Kullanıcı kayıt hatası: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Kayıt işlemi başarısız");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        logger.info("Giriş denemesi: {}", request.getUsername());
        
        // Önce admin tablosunda ara
        Optional<Admin> adminOpt = adminRepo.findById(request.getUsername());
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            logger.info("Admin kullanıcısı bulundu: {}, Rol: {}", admin.getUsername(), admin.getRole());
            
            if (passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
                logger.info("Admin şifre doğrulandı. Token oluşturuluyor, Rol: {}", admin.getRole());
                
                String token = jwtService.generateToken(admin.getUsername(), admin.getRole());
                
                LoginResponse response = new LoginResponse();
                response.setToken(token);
                response.setRole(admin.getRole().name());
                response.setUsername(admin.getUsername());
                response.setUserType("ADMIN");
                
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Admin kullanıcı {} için şifre eşleşmedi", request.getUsername());
            }
        }
        
        // Admin bulunamazsa normal user tablosunda ara
        Optional<User> userOpt = userRepo.findByUsername(request.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            logger.info("Normal kullanıcı bulundu: {}, Onaylandı: {}, Aktif: {}", 
                       user.getUsername(), user.getIsApproved(), user.getIsActive());
            
            if (!user.getIsApproved()) {
                logger.warn("Kullanıcı {} henüz onaylanmamış", request.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Hesabınız henüz admin tarafından onaylanmamış. Lütfen onay bekleyin.");
            }
            
            if (!user.getIsActive()) {
                logger.warn("Kullanıcı {} aktif değil", request.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Hesabınız aktif değil");
            }
            
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                logger.info("Kullanıcı şifre doğrulandı. Token oluşturuluyor, Rol: {}", user.getRole());
                
                // User'ın kendi rolünü Admin.Role enum'ına çevir
                Role adminRole;
                try {
                    adminRole = Role.valueOf(user.getRole().name());
                } catch (IllegalArgumentException e) {
                    // Eğer rol eşleşmezse varsayılan olarak MUHASEBECI
                    adminRole = Role.MUHASEBECI;
                    logger.warn("Kullanıcı rolü {} Admin rolüne çevrilemedi, MUHASEBECI atandı", user.getRole());
                }
                
                String token = jwtService.generateToken(user.getUsername(), adminRole);
                
                LoginResponse response = new LoginResponse();
                response.setToken(token);
                response.setRole(adminRole.name());
                response.setUsername(user.getUsername());
                response.setUserType("USER");
                
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Kullanıcı {} için şifre eşleşmedi", request.getUsername());
            }
        }
        
        logger.warn("Kullanıcı bulunamadı: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Geçersiz kullanıcı adı veya şifre");
    }
    
    // Şifre değiştirme
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            // Önce admin tablosunda ara
            Optional<Admin> adminOpt = adminRepo.findById(request.getUsername());
            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();
                if (passwordEncoder.matches(request.getOldPassword(), admin.getPassword())) {
                    admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
                    adminRepo.save(admin);
                    logger.info("Admin kullanıcı {} şifresini değiştirdi", request.getUsername());
                    return ResponseEntity.ok(Map.of("message", "Şifre başarıyla değiştirildi"));
                } else {
                    return ResponseEntity.badRequest().body("Mevcut şifre yanlış");
                }
            }

            // Admin bulunamazsa user tablosunda ara
            Optional<User> userOpt = userRepo.findByUsername(request.getUsername());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                    userRepo.save(user);
                    logger.info("Normal kullanıcı {} şifresini değiştirdi", request.getUsername());
                    return ResponseEntity.ok(Map.of("message", "Şifre başarıyla değiştirildi"));
                } else {
                    return ResponseEntity.badRequest().body("Mevcut şifre yanlış");
                }
            }

            return ResponseEntity.badRequest().body("Kullanıcı bulunamadı");
        } catch (Exception e) {
            logger.error("Şifre değiştirme hatası: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Şifre değiştirme başarısız");
        }
    }

    // Debug endpoint - kullanıcının hangi tabloda olduğunu kontrol etmek için
    @GetMapping("/debug/user-info/{username}")
    public ResponseEntity<?> getUserInfo(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        
        // Admin tablosunda ara
        Optional<Admin> adminOpt = adminRepo.findById(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            response.put("found", true);
            response.put("table", "admin");
            response.put("username", admin.getUsername());
            response.put("role", admin.getRole().name());
            response.put("isApproved", true);  // Admin tablosu always approved
            response.put("isActive", true);    // Admin tablosu always active
            return ResponseEntity.ok(response);
        }

        // User tablosunda ara
        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            response.put("found", true);
            response.put("table", "users");
            response.put("username", user.getUsername());
            response.put("role", user.getRole().name());
            response.put("isApproved", user.getIsApproved());
            response.put("isActive", user.getIsActive());
            response.put("createdAt", user.getCreatedAt());
            response.put("approvedBy", user.getApprovedBy());
            response.put("approvedAt", user.getApprovedAt());
            return ResponseEntity.ok(response);
        }

        response.put("found", false);
        response.put("message", "Kullanıcı hiçbir tabloda bulunamadı");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/admin/test")
    public ResponseEntity<?> testAdminAuth() {
        logger.info("Admin kimlik doğrulama test endpointi çağrıldı");
        return ResponseEntity.ok(
            Map.of("message", "Admin kimlik doğrulama başarılı", 
                  "timestamp", System.currentTimeMillis()));
    }
    
    // Sadece ADMIN rolüne sahip kullanıcılar tarafından erişilebilir - Usta oluşturma
    @PostMapping("/admin/create-usta")
    public ResponseEntity<?> createUsta(@RequestBody CreateUserRequest request) {
        // Kullanıcı adı kontrolü
        if (adminRepo.existsById(request.getUsername())) {
            return ResponseEntity.badRequest().body("Bu kullanıcı adı zaten kullanılıyor");
        }
        
        Admin usta = new Admin();
        usta.setUsername(request.getUsername());
        usta.setPassword(passwordEncoder.encode(request.getPassword()));
        usta.setRole(Role.USTA);
        
        adminRepo.save(usta);
        logger.info("Yeni usta oluşturuldu: {}", usta.getUsername());
        return ResponseEntity.ok(new AdminResponse(usta));
    }
    
    // Sadece ADMIN rolüne sahip kullanıcılar tarafından erişilebilir - Muhasebeci oluşturma
    @PostMapping("/admin/create-muhasebeci")
    public ResponseEntity<?> createMuhasebeci(@RequestBody CreateUserRequest request) {
        // Kullanıcı adı kontrolü
        if (adminRepo.existsById(request.getUsername())) {
            return ResponseEntity.badRequest().body("Bu kullanıcı adı zaten kullanılıyor");
        }
        
        Admin muhasebeci = new Admin();
        muhasebeci.setUsername(request.getUsername());
        muhasebeci.setPassword(passwordEncoder.encode(request.getPassword()));
        muhasebeci.setRole(Role.MUHASEBECI);
        
        adminRepo.save(muhasebeci);
        logger.info("Yeni muhasebeci oluşturuldu: {}", muhasebeci.getUsername());
        return ResponseEntity.ok(new AdminResponse(muhasebeci));
    }
    
    // Tüm kullanıcıları listele (sadece ADMIN erişebilir)
    @GetMapping("/admin/users")
    public ResponseEntity<List<AdminResponse>> listAllUsers() {
        List<Admin> allUsers = adminRepo.findAll();
        List<AdminResponse> response = allUsers.stream()
                .map(AdminResponse::new)
                .collect(Collectors.toList());
        logger.info("{} adet kullanıcı listelendi", allUsers.size());
        return ResponseEntity.ok(response);
    }
    
    // Belirli role sahip kullanıcıları listele (sadece ADMIN erişebilir)
    @GetMapping("/admin/users/{role}")
    public ResponseEntity<List<AdminResponse>> listUsersByRole(@PathVariable String role) {
        try {
            Role userRole = Role.valueOf(role.toUpperCase());
            List<Admin> users = adminRepo.findByRole(userRole);
            List<AdminResponse> response = users.stream()
                    .map(AdminResponse::new)
                    .collect(Collectors.toList());
            logger.info("{} rolünde {} adet kullanıcı listelendi", role, users.size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Geçersiz rol: {}", role);
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }
    
    // Kullanıcı silme (sadece ADMIN erişebilir, ADMIN kullanıcıları silinemez)
    @DeleteMapping("/admin/users/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        Optional<Admin> userOpt = adminRepo.findById(username);
        
        if (userOpt.isEmpty()) {
            logger.warn("Silinecek kullanıcı bulunamadı: {}", username);
            return ResponseEntity.notFound().build();
        }
        
        Admin user = userOpt.get();
        if (user.getRole() == Role.ADMIN) {
            logger.warn("ADMIN rolündeki hesaplar silinemez: {}", username);
            return ResponseEntity.badRequest().body("ADMIN hesapları silinemez");
        }
        
        adminRepo.delete(user);
        logger.info("Kullanıcı silindi: {} ({})", username, user.getRole());
        return ResponseEntity.ok().build();
    }

    // Onay bekleyen kullanıcıları listele (sadece ADMIN erişebilir)
    @GetMapping("/admin/pending-users")
    public ResponseEntity<List<UserResponse>> listPendingUsers() {
        List<User> pendingUsers = userRepo.findByIsApprovedFalseOrderByCreatedAtDesc();
        List<UserResponse> response = pendingUsers.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
        logger.info("{} adet onay bekleyen kullanıcı listelendi", pendingUsers.size());
        return ResponseEntity.ok(response);
    }

    // Onay bekleyen kullanıcı sayısını getir (sadece ADMIN erişebilir)
    @GetMapping("/admin/pending-users/count")
    public ResponseEntity<Map<String, Object>> getPendingUsersCount() {
        long count = userRepo.countByIsApprovedFalse();
        return ResponseEntity.ok(Map.of("pendingCount", count));
    }

    // Kullanıcıyı onayla (sadece ADMIN erişebilir)
    @PostMapping("/admin/approve-user/{userId}")
    public ResponseEntity<?> approveUser(@PathVariable Long userId, @RequestBody ApproveUserRequest request) {
        Optional<User> userOpt = userRepo.findById(userId);
        
        if (userOpt.isEmpty()) {
            logger.warn("Onaylanacak kullanıcı bulunamadı: {}", userId);
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        if (user.getIsApproved()) {
            return ResponseEntity.badRequest().body("Kullanıcı zaten onaylı");
        }
        
        user.setIsApproved(true);
        user.setIsActive(true);
        user.setApprovedBy(request.getApprovedBy());
        user.setApprovedAt(LocalDateTime.now());
        
        userRepo.save(user);
        logger.info("Kullanıcı onaylandı: {} (Onaylayan: {})", user.getUsername(), request.getApprovedBy());
        
        return ResponseEntity.ok(Map.of(
            "message", "Kullanıcı başarıyla onaylandı",
            "user", new UserResponse(user)
        ));
    }

    // Kullanıcı onayını reddet (sadece ADMIN erişebilir)
    @DeleteMapping("/admin/reject-user/{userId}")
    public ResponseEntity<?> rejectUser(@PathVariable Long userId) {
        Optional<User> userOpt = userRepo.findById(userId);
        
        if (userOpt.isEmpty()) {
            logger.warn("Reddedilecek kullanıcı bulunamadı: {}", userId);
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        userRepo.delete(user);
        logger.info("Kullanıcı kaydı reddedildi ve silindi: {}", user.getUsername());
        
        return ResponseEntity.ok(Map.of("message", "Kullanıcı kaydı reddedildi"));
    }

    @Getter @Setter
    static class RegisterRequest {
        private String username;
        private String password;
        private String fullName;
        private String email;
        private String phone;
        private String role; // Seçilen rol
        
        // Manual getters/setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    @Getter @Setter
    static class LoginRequest {
        private String username;
        private String password;
        
        // Manual getters/setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    @Getter @Setter
    static class CreateUserRequest {
        private String username;
        private String password;
        
        // Manual getters/setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    @Getter @Setter
    static class ChangePasswordRequest {
        private String username;
        private String oldPassword; // Mevcut şifre
        private String newPassword;
        
        // Manual getters/setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
    
    @Getter @Setter
    static class LoginResponse {
        private String token;
        private String role;
        private String username;
        private String userType; // "ADMIN" veya "USER"
        
        // Manual getters/setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getUserType() { return userType; }
        public void setUserType(String userType) { this.userType = userType; }
    }
    
    @Getter @Setter
    static class AdminResponse {
        private String username;
        private String role;
        
        public AdminResponse(Admin admin) {
            this.username = admin.getUsername();
            this.role = admin.getRole().name();
        }
        
        // Manual getters/setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    @Getter @Setter
    static class ApproveUserRequest {
        private String approvedBy;
        
        // Manual getters/setters
        public String getApprovedBy() { return approvedBy; }
        public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    }

    @Getter @Setter
    static class UserResponse {
        private Long id;
        private String username;
        private String fullName;
        private String email;
        private String phone;
        private String role;
        private LocalDateTime createdAt;
        private Boolean isApproved;
        private String approvedBy;
        private LocalDateTime approvedAt;
        
        public UserResponse(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.fullName = user.getFullName();
            this.email = user.getEmail();
            this.phone = user.getPhone();
            this.role = user.getRole().name();
            this.createdAt = user.getCreatedAt();
            this.isApproved = user.getIsApproved();
            this.approvedBy = user.getApprovedBy();
            this.approvedAt = user.getApprovedAt();
        }
    }
}
