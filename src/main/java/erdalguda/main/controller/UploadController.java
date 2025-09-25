package erdalguda.main.controller;

import erdalguda.main.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    private final S3Service s3Service;
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    public UploadController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam(value = "file", required = false) MultipartFile file,
                                         @RequestParam(value = "image", required = false) MultipartFile image) {
        logger.info("Upload endpoint'ine istek geldi");
        
        try {
            MultipartFile fileToUpload = file != null ? file : image;
            if (fileToUpload == null) {
                logger.warn("Dosya bulunamadı");
                return ResponseEntity.badRequest().body("Dosya bulunamadı. 'file' veya 'image' parametresi gerekli.");
            }
            
            logger.info("Dosya yükleniyor: {} - {} bytes", fileToUpload.getOriginalFilename(), fileToUpload.getSize());
            String url = s3Service.uploadFile(fileToUpload);
            logger.info("S3'e başarıyla yüklendi: {}", url);
            
            return ResponseEntity.ok().body(java.util.Map.of("url", url));
        } catch (Exception e) {
            logger.error("Yükleme hatası: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Yükleme hatası: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        logger.info("Upload test endpoint çağrıldı");
        return ResponseEntity.ok("Upload endpoint çalışıyor!");
    }
} 