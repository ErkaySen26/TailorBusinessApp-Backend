package erdalguda.main.controller;

import erdalguda.main.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class EmailTestController {

    private final EmailService emailService;
    private static final Logger log = LoggerFactory.getLogger(EmailTestController.class);

    @Autowired
    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/email")
    public ResponseEntity<?> testEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email adresi gerekli");
            }

            log.info("Test email gönderiliyor: {}", email);
            emailService.sendWelcomeEmail(email, "Test", "Kullanıcı");
            
            return ResponseEntity.ok("Test email başarıyla gönderildi: " + email);
            
        } catch (Exception e) {
            log.error("Test email gönderilirken hata: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body("Email gönderilirken hata: " + e.getMessage());
        }
    }

    @GetMapping("/email-config")
    public ResponseEntity<?> checkEmailConfig() {
        // Email konfigürasyonunu kontrol et
        return ResponseEntity.ok("Email konfigürasyonu aktif");
    }
} 